package com.starshop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
