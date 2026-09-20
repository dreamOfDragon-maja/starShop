package com.starshop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponCreateDTO {

    @NotBlank(message = "活动名称不能为空")
    //优惠券活动名称
    private String activityName;

    @NotNull(message = "优惠券类型不能为空")
    //优惠券类型：1满减 2折扣 3无门槛 4单品券
    private Integer couponType;

    //满减/无门槛面额，折扣券传null
    private BigDecimal faceValue;

    //折扣比例，例8.8=88折，非折扣券传null
    private BigDecimal discountRate;

    //折扣最高优惠上限
    private BigDecimal maxDiscount;

    @NotNull(message = "最低消费门槛不能为空")
    //使用最低消费门槛，0=无门槛
    private BigDecimal minSpend;

    @NotNull(message = "总发行量不能为空")
    //总发放库存，0不限量
    private Integer totalQuota;

    @NotNull(message = "有效期模式不能为空")
    //有效期模式：1固定时间 2领券后N天有效
    private Integer validMode;

    //固定有效期开始时间，模式2传null
    private LocalDateTime validStart;

    //固定有效期结束时间，模式2传null
    private LocalDateTime validEnd;

    //领券后有效天数，模式1传null
    private Integer receiveValidDays;

    @NotNull(message = "单人限领张数不能为空")
    //单人限领张数
    private Integer limitPerPerson;

    @NotNull(message = "用户限制类型不能为空")
    //用户限制：1全部 2新人 3会员 4指定人群
    private Integer userLimitType;

    @NotNull(message = "使用范围不能为空")
    //使用范围：1全场 2指定商品 3指定分类
    private Integer useScope;

    //互斥组ID，0无互斥
    private Long mutexGroupId;

    @NotNull(message = "活动状态不能为空")
    //活动状态：0未开始 1发放中 2已结束 3作废
    private Integer status;

    @NotNull(message = "发行时间不能为空")
    //优惠券发行时间
    private LocalDateTime releaseTime;
}