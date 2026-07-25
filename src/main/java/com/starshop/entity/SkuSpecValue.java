package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SKU-规格值关联表 - star_sku_spec_value
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("star_sku_spec_value")
public class SkuSpecValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 规格ID */
    private Long specId;

    /** 规格值ID */
    private Long specValueId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
