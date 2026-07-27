package com.starshop.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {

    private Long id;
    private String token;

}
