package com.starshop.controller.admin;

import com.starshop.pojo.dto.CouponCreateDTO;
import com.starshop.result.Result;
import com.starshop.service.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 管理端添加优惠券
     * @param couponCreateDTO
     * @return
     */
    @PostMapping("/admin/coupon/release")
    public Result<?> saveCouponAdmin(@RequestBody @Valid @NotNull CouponCreateDTO couponCreateDTO) {
        return couponService.saveCouponAdmin(couponCreateDTO);
    }
}
