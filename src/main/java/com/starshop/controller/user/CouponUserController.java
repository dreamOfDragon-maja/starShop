package com.starshop.controller.user;

import com.starshop.common.result.Result;
import com.starshop.service.CouponService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端优惠券接口(领券中心)
 */
@RestController
@RequestMapping("/api/coupon")
public class CouponUserController {

    @Resource
    private CouponService couponService;

    /**
     * 领券中心的优惠券列表
     * @return 发放中的优惠券（带当前用户的领取情况）
     */
    @GetMapping("/list")
    public Result getCouponList() {
        return couponService.getClaimableCouponList();
    }

    /**
     * 领取优惠券
     * @param couponId 优惠券模板 id
     * @return 领取结果
     */
    @PostMapping("/receive")
    public Result receiveCoupon(@RequestParam("couponId") Long couponId) {
        return couponService.receiveCoupon(couponId);
    }
}
