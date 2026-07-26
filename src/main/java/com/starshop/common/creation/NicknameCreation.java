package com.starshop.common.creation;


import java.util.UUID;

public class NicknameCreation {
    private static final String PREFIX_USER = "用户";
    private static final String PREFIX_ANONYMOUS_USER = "匿名用户";

    /**
     * 生成默认昵称（无重复校验）
     */
    public static String createDefaultNickname() {
        String randomStr = UUID.randomUUID().toString().substring(0,6);
        return  PREFIX_USER+ randomStr;
    }

    public static String createAnonymousNickname() {
        String randomStr =UUID.randomUUID().toString().substring(0,6);
        return  PREFIX_ANONYMOUS_USER+ randomStr;
    }



}
