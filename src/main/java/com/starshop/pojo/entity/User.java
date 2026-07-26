package com.starshop.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)  // 自增主键
    private Long id;

    private String username;

    private String password;

    @Builder.Default
    private String nickname = "";

    @Builder.Default
    private String avatar = "/static/images/default-avatar.png";

    private String phone;

    private String openid;

    /**
     * 用户类型：1-管理员，2-商家，3-普通用户（默认）
     */
    @Builder.Default
    private Integer userType = 3;

    /**
     * 是否启用：0-禁用，1-启用（默认）
     */
    @Builder.Default
    private Integer isEnable = 1;

    private LocalDateTime firstLoginTime;

    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}