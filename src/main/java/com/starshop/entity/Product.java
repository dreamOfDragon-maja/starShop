package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SPU表 - star_product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_product")
public class Product extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 副标题/卖点 */
    private String subtitle;

    /** 品牌 */
    private String brand;

    /** 商品描述（富文本HTML） */
    private String description;

    /** 商品主图URL */
    private String mainImage;

    /** 最低销售价 */
    private BigDecimal price;

    /** 划线原价 */
    private BigDecimal originalPrice;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 总库存（冗余） */
    private Integer stock;

    /** 总销量（冗余） */
    private Integer sales;

    /** 计量单位 */
    private String unit;

    /** 重量（千克） */
    private BigDecimal weight;

    /** 状态：0=下架, 1=上架, 2=草稿 */
    private Integer status;

    /** 是否新品：0=否, 1=是 */
    private Integer isNew;

    /** 是否热销：0=否, 1=是 */
    private Integer isHot;

    /** 是否推荐：0=否, 1=是 */
    private Integer isRecommend;

    /** 排序 */
    private Integer sortOrder;
}
