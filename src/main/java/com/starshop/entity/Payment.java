package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表 - star_payment
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付单号（唯一） */
    private String paymentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号（冗余） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付金额（元） */
    private BigDecimal amount;

    /** 支付方式：1=微信支付, 2=支付宝 */
    private Integer payType;

    /** 支付状态：0=待支付, 1=支付成功, 2=支付失败, 3=已退款 */
    private Integer status;

    /** 第三方交易流水号 */
    private String tradeNo;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 退款原因 */
    private String refundReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
