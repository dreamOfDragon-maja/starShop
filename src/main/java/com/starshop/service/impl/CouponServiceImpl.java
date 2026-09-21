package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.creation.SnowflakeIdGenerator;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.CouponMapper;
import com.starshop.pojo.dto.CouponCreateDTO;
import com.starshop.pojo.entity.Coupon;
import com.starshop.pojo.entity.CouponUser;
import com.starshop.pojo.enums.CouponUseStatusEnum;
import com.starshop.pojo.enums.CouponValidModeEnum;
import com.starshop.result.Result;
import com.starshop.service.CouponService;
import com.starshop.service.CouponUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CopyMapper copyMapper;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private final CouponMapper couponMapper;

    private final CouponUserService couponUserService;

    /**
     * 管理端添加优惠券
     * @param couponCreateDTO
     * @return
     */
    @Override
    public Result<?> saveCouponAdmin(CouponCreateDTO couponCreateDTO) {
        Coupon coupon = copyMapper.couponCreateDTOToCoupon(couponCreateDTO);
        //雪花算法生成优惠券编码
        coupon.setCouponNo(snowflakeIdGenerator.generateCouponNo());
        boolean isSuccess = save(coupon);
        if (!isSuccess) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        return Result.success();
    }

    /**
     * 初始化更新优惠券缓存
     */
    @Override
    public void updateCouponRedisCache() {
        //多表查询
        List<Coupon> couponList = couponMapper.selectCouponWithMutexCroupAndScopeDetail();

        //批量写入缓存
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : couponList) {
                    Long couponId = coupon.getId();
                    String key = RedisKeyConstant.PREFIX_COUPON + couponId + ":" + RedisKeyConstant.DETAIL;;
                    RedisConnector.setHashObject(key, coupon);
                }
                return null;
            }
        });
        List<Long> couponIdList = couponList.stream().map(Coupon::getId).toList();

        if (couponIdList.isEmpty()) {
            return;
        }
        //查询所有拥有优惠券的用户，并且根据优惠券id分组
        Map<Long, List<CouponUser>> couponUserMap = couponUserService.lambdaQuery().in(CouponUser::getCouponId, couponIdList).list()
                .stream().collect(Collectors.groupingBy(CouponUser::getCouponId));
        //为优惠券设置用户个人优惠券的具体信息
        couponList.forEach(coupon -> coupon.setCouponUserList(couponUserMap.get(coupon.getId())));
        //筛选固定时间类型的优惠券
        List<Coupon> couponFixedTimeList = couponList.stream()
                .filter(coupon -> Objects.equals(coupon.getValidMode(), CouponValidModeEnum.FIXED_TIME.getCode()))
                .toList();
        updateCouponFixedTimeListCache(couponFixedTimeList);
        //筛选非固定时间的优惠券
        List<Coupon> couponAfterReceiveList = couponList.stream().filter(coupon -> Objects.equals(coupon.getValidMode(), CouponValidModeEnum.AFTER_RECEIVE.getCode()))
                .toList();
        updateAfterReceiveListCache(couponAfterReceiveList);
    }


    /**
     * 更新固定时间优惠券缓存
     * 维护 ZSet 存储优惠券id 监控状态变化 , 维护 Set 存储用户 id
     * @param couponList 固定时间优惠券列表
     */
    private void updateCouponFixedTimeListCache(List<Coupon> couponList) {
        if (couponList == null || couponList.isEmpty()) {
            return;
        }
        for (Coupon coupon : couponList) {
            //优惠券起始时间
            LocalDateTime validStart = coupon.getValidStart();
            LocalDateTime validEnd = coupon.getValidEnd();
            LocalDateTime now = LocalDateTime.now();
            Long couponId = coupon.getId();
            //拥有这张券的用户集合
            List<CouponUser> couponUserList = coupon.getCouponUserList();

            //未使用
            if (now.isBefore(validStart)) {
                //服务器默认时区毫秒时间戳
                long timestamp = validStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                String key = RedisKeyConstant.PREFIX_COUPON + RedisKeyConstant.COUPON_FIXED_TIME_UN_BEGIN;
                //缓存未开始的优惠券id，监控
                RedisConnector.opsForZSet().add(key, couponId, timestamp);
                String userIdListSetKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.UN_BEGIN);
                //缓存拥有这张券的用户id集合，distinct（）：去重
                RedisConnector.safeAddToSet(userIdListSetKey, couponUserList.stream()
                        .map(CouponUser::getUserId).distinct().toArray(Object[]::new));
                //已过期
            } else if (now.isAfter(validEnd)) {
                HashSet<Long> used = new HashSet<>();
                HashSet<Long> expired = new HashSet<>();
                for (CouponUser couponUser : couponUserList) {
                    //未开始，过期，退回统一判定为过期
                    switch (CouponUseStatusEnum.getByCode(couponUser.getUseStatus())) {
                        case UN_BEGIN, EXPIRED, RETURNED -> expired.add(couponUser.getUserId());
                        case USED -> used.add(couponUser.getUserId());
                    }
                }
                String expireKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.EXPIRED);
                String usedKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.USED);

                RedisConnector.safeAddToSet(expireKey, expired.toArray(new Object[0]));
                RedisConnector.safeAddToSet(usedKey, used.toArray(new Object[0]));
            //有效期内
            } else {
                long timestamp = validEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                String key = RedisKeyConstant.PREFIX_COUPON + RedisKeyConstant.COUPON_FIXED_TIME_IN_PROGRESS;
                //缓存在有效期内的优惠券，监控
                RedisConnector.opsForZSet().add(key, couponId, timestamp);
                //分为未使用，已使用，退回三个set
                Set<Long> unused = new HashSet<>();
                Set<Long> used = new HashSet<>();
                Set<Long> returned = new HashSet<>();
                for (CouponUser couponUser : couponUserList) {
                    switch (CouponUseStatusEnum.getByCode(couponUser.getUseStatus())) {
                        case UN_BEGIN, UNUSED -> unused.add(couponUser.getUserId());
                        case USED -> used.add(couponUser.getUserId());
                        case RETURNED -> returned.add(couponUser.getUserId());
                    }
                }
                String unusedKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.UNUSED);
                String usedKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.USED);
                String returnedKey = couponUseStatusIdSet(couponId, CouponUseStatusEnum.RETURNED);

                RedisConnector.safeAddToSet(unusedKey, unused.toArray(new Object[0]));
                RedisConnector.safeAddToSet(returnedKey, returned.toArray(new Object[0]));
                RedisConnector.safeAddToSet(usedKey, used.toArray(new Object[0]));
            }
        }
    }


    /**
     * 更新领劵后 N 天 优惠券缓存
     * 维护 ZSet (其中存储 couponUserId)监控状态变化 和 Set 存储用户 id
     * @param couponList 领券后 N 天优惠券列表
     */
    private void updateAfterReceiveListCache(List<Coupon> couponList) {
        //过滤掉已使用和已退回的优惠券
        filterCouponUserStatus(couponList, CouponUseStatusEnum.USED);
        filterCouponUserStatus(couponList, CouponUseStatusEnum.RETURNED);
        //再次过滤
        List<Coupon> userStatusFilterList = couponList.stream().peek(coupon -> {
            List<CouponUser> couponUserList = coupon.getCouponUserList().stream()
                    .filter(couponUser -> couponUser.getUseStatus().equals(CouponUseStatusEnum.UN_BEGIN.getCode())
                            || couponUser.getUseStatus().equals(CouponUseStatusEnum.UNUSED.getCode())
                            || couponUser.getUseStatus().equals(CouponUseStatusEnum.EXPIRED.getCode())).toList();
            coupon.setCouponUserList(couponUserList);
        }).toList();

        //批量处理
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : userStatusFilterList) {
                    HashSet<Long> unused = new HashSet<>();
                    HashSet<Long> unBegin = new HashSet<>();
                    HashSet<Long> expired = new HashSet<>();
                    for (CouponUser couponUser : coupon.getCouponUserList()) {
                        switch (CouponUseStatusEnum.getByCode(coupon.getStatus())) {
                            case UN_BEGIN -> {
                                unBegin.add(couponUser.getUserId());
                                updateCouponUserRedisCache(couponUser);
                            }
                            case UNUSED -> {
                                unused.add(couponUser.getUserId());
                                updateCouponUserRedisCache(couponUser);
                            }
                            default -> expired.add(couponUser.getUserId());
                        }
                    }
                    Long couponId = coupon.getId();
                    RedisConnector.safeAddToSet(couponUseStatusIdSet(couponId, CouponUseStatusEnum.UNUSED), unused.toArray(new Object[0]));
                    RedisConnector.safeAddToSet(couponUseStatusIdSet(couponId, CouponUseStatusEnum.UN_BEGIN), unBegin.toArray(new Object[0]));
                    RedisConnector.safeAddToSet(couponUseStatusIdSet(couponId, CouponUseStatusEnum.EXPIRED), expired.toArray(new Object[0]));
                }
                return null;
            }
        });
    }


    /**
     * 过滤用户优惠券
     * @param couponList
     * @param couponUseStatusEnum
     */
    private static void filterCouponUserStatus(List<Coupon> couponList, CouponUseStatusEnum couponUseStatusEnum){
        //过滤
        List<Coupon> usedList = couponList.stream().peek(coupon -> {
            List<CouponUser> couponUserList = coupon.getCouponUserList()
                    .stream().filter(couponUser -> couponUser.getUseStatus().equals(couponUseStatusEnum.getCode())).toList();
            coupon.setCouponUserList(couponUserList);
        }).toList();

        //批量写入缓存
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : usedList) {
                    Long couponId = coupon.getId();
                    String key = couponUseStatusIdSet(couponId, couponUseStatusEnum);

                    RedisConnector.safeAddToSet(key, coupon.getCouponUserList().stream()
                            .map(CouponUser::getUserId).distinct().toArray(Object[]::new));
                }
                return null;
            }
        });
    }

    /**
     * 更新 N 天后过期的优惠券的 ZSet 缓存 其中存储couponUserId
     * @param couponUser 用户持有的优惠券
     */
    private void updateCouponUserRedisCache(CouponUser couponUser) {
        Long couponUserId = couponUser.getCouponId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = couponUser.getValidStart();
        LocalDateTime end = couponUser.getValidEnd();
        //未开始
        if (now.isBefore(start)) {
            String key = RedisKeyConstant.PREFIX_COUPON  + ":" + RedisKeyConstant.AFTER_RECEIVE_TIME_UN_BEGIN;
            long timestamp = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            RedisConnector.opsForZSet().add(key, couponUserId, timestamp);
        }
        //有效期内
        if (now.isAfter(start) && now.isBefore(end)) {
            String key = RedisKeyConstant.PREFIX_COUPON + RedisKeyConstant.AFTER_RECEIVE_TIME_IN_PROGRESS;
            long timestamp = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            RedisConnector.opsForZSet().add(key, couponUserId, timestamp);
        }
    }

    /**
     * couponUseStatus
     * coupon: + couponId + : + couponUseStatus: + key + idList
     * @param couponId
     * @param couponUseStatusEnum
     * @return
     */
    public static String couponUseStatusIdSet(Long couponId, CouponUseStatusEnum couponUseStatusEnum) {
        return RedisKeyConstant.PREFIX_COUPON + couponId + ":" + RedisKeyConstant.USE_STATUS + couponUseStatusEnum.getKey() + RedisKeyConstant.ID_LIST;
    }
}
