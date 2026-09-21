package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.CouponCreateDTO;
import com.starshop.pojo.entity.Coupon;
import com.starshop.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CouponService extends IService<Coupon> {
    /**
     * 管理端添加优惠券
     * @param couponCreateDTO
     * @return
     */
    Result<?> saveCouponAdmin(@Valid @NotNull CouponCreateDTO couponCreateDTO);

    /**
     * 更新优惠券缓存
     */
    void updateCouponRedisCache();
}
