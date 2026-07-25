package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单物流表 - star_order_logistics
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_order_logistics")
public class OrderLogistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单编号（冗余） */
    private String orderNo;

    /** 物流公司名称 */
    private String logisticsCompany;

    /** 物流单号 */
    private String logisticsNo;

    /** 物流状态：0=待发货, 1=已揽件, 2=运输中, 3=派送中, 4=已签收 */
    private Integer status;

    /** 物流追踪详情（JSON） */
    private String trackingInfo;

    /** 发货时间 */
    private LocalDateTime sendTime;

    /** 签收时间 */
    private LocalDateTime receiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
