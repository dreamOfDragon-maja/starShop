package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品图片表 - star_product_image
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 图片URL */
    private String imageUrl;

    /** 排序 */
    private Integer sortOrder;

    /** 是否主图：0=否, 1=是 */
    private Integer isMain;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
