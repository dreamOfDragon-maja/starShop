package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品规格表 - star_product_spec
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_product_spec")
public class ProductSpec {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 规格名称（如：颜色、尺寸） */
    private String name;

    /** 规格类型：1=规格参数（仅展示）, 2=SKU规格（生成SKU） */
    private Integer type;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
