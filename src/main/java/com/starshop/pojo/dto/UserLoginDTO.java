package com.starshop.pojo.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;
    private String password;
    private String phone;
}
