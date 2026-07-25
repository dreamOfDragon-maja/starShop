package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台角色表 - star_admin_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_admin_role")
public class AdminRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色编码（如：super_admin, product_manager） */
    private String code;

    /** 角色描述 */
    private String description;

    /** 权限集合（JSON数组） */
    private String permissions;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
