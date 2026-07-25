package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类表 - star_category
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_category")
public class Category extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类ID，0=顶级分类 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 分类层级：1=一级, 2=二级, 3=三级 */
    private Integer level;

    /** 排序 */
    private Integer sortOrder;

    /** 分类图标URL */
    private String icon;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
