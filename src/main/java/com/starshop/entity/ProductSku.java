package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU表 - star_product_sku
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_product_sku")
public class ProductSku extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品SPU ID */
    private Long productId;

    /** SKU编码 */
    private String skuCode;

    /** SKU名称（冗余，如：iPhone 14 Pro 256GB 深空黑） */
    private String name;

    /** 规格描述（冗余，如：颜色:深空黑;容量:256GB） */
    private String specDesc;

    /** 销售价 */
    private BigDecimal price;

    /** 划线原价 */
    private BigDecimal originalPrice;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** SKU专属图片URL */
    private String image;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
