package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品评价表 - star_review
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_review")
public class Review extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评价用户ID */
    private Long userId;

    /** 订单ID */
    private Long orderId;

    /** 订单编号（冗余） */
    private String orderNo;

    /** 商品SPU ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 评分：1~5星 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片（JSON数组） */
    private String images;

    /** 是否匿名：0=否, 1=是 */
    private Integer isAnonymous;

    /** 商家回复内容 */
    private String replyContent;

    /** 商家回复时间 */
    private LocalDateTime replyTime;

    /** 状态：0=隐藏, 1=显示 */
    private Integer status;
}
