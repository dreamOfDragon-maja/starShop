package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表 - star_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 性别：0=未知, 1=男, 2=女 */
    private Integer gender;

    /** 生日 */
    private LocalDate birthday;

    /** 状态：0=禁用, 1=正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}
