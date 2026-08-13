package com.starshop.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 使用于用户微信快捷登录
 */
@Data
public class UserWechatDTO implements Serializable {
    //微信登录 code
    private String code;
    //微信昵称
    private String nickName;
    //微信头像链接
    private String avatarUrl;
}
