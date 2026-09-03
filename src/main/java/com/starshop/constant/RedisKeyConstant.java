package com.starshop.constant;

public class RedisKeyConstant {

    /**
     * 登录模块
     */
    public static final String TOKEN = "token:";
    public static final String PREFIX_LOGIN = "login:";
    public static final String REFRESH = "refresh";
    public static final String USER_ID = "userId";
    public static final String FIELD_ACCESS_TOKEN = "accessToken";
    public static final String FIELD_REFRESH_TOKEN = "refreshToken";
    public static final String USER = "user";

    /**
     * 分类模块
     */
    public static final String PREFIX_CATEGORY = "category:";
    public static final String TREE = "tree";
    public static final String FIRST_CATEGORY = "firstCategory:";

    /**
     * 商品模块
     */
    public static final String HOT = "hot";
    public static final String PREFIX_PRODUCT = "product:";
    public static final String PREFIX_COPY = "copy:";
    public static final String ID_LIST="idList";


    /**
     * Bucket 读写标记前缀（仅用于 Redisson RBucket 锁标记，不可与业务数据 key 混用）
     */
    public static final String BUCKET_SIGN_PREFIX = "bucket:sign:";

}
