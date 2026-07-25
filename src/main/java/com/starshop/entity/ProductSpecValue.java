package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 规格值表 - star_product_spec_value
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_product_spec_value")
public class ProductSpecValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规格ID */
    private Long specId;

    /** 规格值（如：深空黑、XL、256GB） */
    private String value;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
