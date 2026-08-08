package com.starshop.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private String nickname;
    private String avatar;
    private String phone;
}
