package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.CouponCreateDTO;
import com.starshop.pojo.entity.Coupon;
import com.starshop.pojo.vo.CouponVO;
import com.starshop.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

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

    /**
     * 查询领券中心的优惠券列表(发放中的券,并带上当前用户的领取情况)
     * @return 优惠券列表
     */
    Result<List<CouponVO>> getClaimableCouponList();

    /**
     * 用户领取优惠券
     * @param couponId 优惠券模板 id
     * @return 领取结果
     */
    Result<?> receiveCoupon(Long couponId);
}
