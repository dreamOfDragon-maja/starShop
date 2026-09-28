package com.starshop.pojo.vo;

import lombok.Data;

@Data
public class OrderAddressVO {

    /**
     * 收件人姓名
     */
    private String receiver;

    /**
     * 收件人手机号
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址（街道/门牌号）
     */
    private String detailAddress;

}
