package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表 - star_order
 *
 * status 流转：
 *   0(待支付) → 1(已支付/待发货) → 2(已发货) → 3(已完成)
 *   0 → 4(已取消)
 *   1/2/3 → 5(退款中) → 6(已退款)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_order")
public class Order extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    // ==== 金额 ====

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    // ==== 支付 ====

    /** 支付方式：1=微信支付, 2=支付宝 */
    private Integer payType;

    /** 支付时间 */
    private LocalDateTime payTime;

    // ==== 状态 ====

    /** 订单状态：0=待支付, 1=已支付/待发货, 2=已发货, 3=已完成, 4=已取消, 5=退款中, 6=已退款 */
    private Integer status;

    /** 发货时间 */
    private LocalDateTime deliveryTime;

    /** 收货/完成时间 */
    private LocalDateTime receiveTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 取消原因 */
    private String cancelReason;

    // ==== 收货地址快照 ====

    /** 收货人姓名（快照） */
    private String receiverName;

    /** 收货人电话（快照） */
    private String receiverPhone;

    /** 省份（快照） */
    private String receiverProvince;

    /** 城市（快照） */
    private String receiverCity;

    /** 区县（快照） */
    private String receiverDistrict;

    /** 详细地址（快照） */
    private String receiverDetail;

    // ==== 备注 ====

    /** 买家备注 */
    private String buyerNote;

    /** 商家备注 */
    private String adminNote;
}
