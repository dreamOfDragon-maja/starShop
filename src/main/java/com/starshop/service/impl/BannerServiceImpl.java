package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveBannerRedisCacheAnnotation;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.BannerMapper;
import com.starshop.pojo.dto.BannerDTO;
import com.starshop.pojo.entity.Banner;
import com.starshop.pojo.enums.BannerStatus;
import com.starshop.result.Result;
import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Resource
    private CopyMapper copyMapper;

    private static final String DELETE_ID = "deleteId";

    /**
     * 获取首页联播图列表
     * @return
     */
    @Override
    public Result<List<Banner>> getBannerList() {
        //查询缓存
        String bannerKey = RedisKeyConstant.PREFIX_BANNER + RedisKeyConstant.ALL;
        List<Object> bannerList = RedisConnector.opsForList().range(bannerKey, 0, -1);

        if (Objects.isNull(bannerList) || bannerList.isEmpty()) {
            List<Banner> banners = lambdaQuery()
                    .orderByAsc(Banner::getSort) //0，1，2，3
                    .eq(Banner::getStatus, BannerStatus.ACTIVE)
                    .list();
            //更新缓存
            RedisConnector.delete(bannerKey);
            //链表右侧放入，3，2，1，0
            RedisConnector.opsForList().rightPushAll(bannerKey, banners.toArray());
            return Result.success(banners);
        }
        //反转结果
        Collections.reverse(bannerList);
        List<Banner> resultList = bannerList.stream().map(object -> (Banner) object).toList();
        return Result.success(resultList);

    }

    /**
     * 用于 admin 获取联播图列表
     */
    @Override
    public Result<List<Banner>> getBannerListAdmin() {
        List<Banner> bannerList = lambdaQuery().orderByAsc(Banner::getSort).list();
        return Result.success(bannerList);
    }

    /**
     * admin 添加 banner
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result addBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        boolean isSuccess = save(banner);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(banner);
    }

    /**
     * admin 修改 banner
     * @param bannerDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result updateBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        boolean isSuccess = updateById(banner);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(banner);
    }

    /**
     * admin 删除 banner
     * @param id
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result deleteBanner(Long id) {
        boolean isSuccess = removeById(id);
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        HashMap<String, Object> resultMap = new HashMap<>(1);
        resultMap.put(DELETE_ID, id);
        return Result.success(resultMap);
    }
}