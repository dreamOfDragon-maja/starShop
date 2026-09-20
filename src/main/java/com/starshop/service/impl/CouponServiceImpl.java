package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.creation.SnowflakeIdGenerator;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.mapper.CouponMapper;
import com.starshop.pojo.dto.CouponCreateDTO;
import com.starshop.pojo.entity.Coupon;
import com.starshop.result.Result;
import com.starshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CopyMapper copyMapper;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

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
}
