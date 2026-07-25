package com.starshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址表 - star_address
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("star_address")
public class Address extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 邮政编码 */
    private String postalCode;

    /** 是否默认地址：0=否, 1=是 */
    private Integer isDefault;
}
