package com.starshop.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.starshop.constant.DatePatternConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领券中心展示用的优惠券
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponVO {

    /**
     * 优惠券模板 id
     */
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 1满减 2折扣 3无门槛 4单品券
     */
    private Integer couponType;

    /**
     * 满减/无门槛金额
     */
    private BigDecimal faceValue;

    /**
     * 折扣率 0.88 = 88折
     */
    private BigDecimal discountRate;

    /**
     * 折扣上限
     */
    private BigDecimal maxDiscount;

    /**
     * 使用门槛
     */
    private BigDecimal minSpend;

    /**
     * 1固定时间 2领券后N天
     */
    private Integer validMode;

    /**
     * 固定有效期开始
     */
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime validStart;

    /**
     * 固定有效期结束
     */
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime validEnd;

    /**
     * 领券后有效天数
     */
    private Integer receiveValidDays;

    /**
     * 总发行量 0不限
     */
    private Integer totalQuota;

    /**
     * 已领取数
     */
    private Integer receiveQuota;

    /**
     * 单人限领
     */
    private Integer limitPerPerson;

    /**
     * 现在还能不能领
     */
    private Boolean canReceive;

    /**
     * 是否已被领完
     */
    private Boolean soldOut;

    /**
     * 当前用户已经领了几张
     */
    private Integer userReceivedCount;
}
