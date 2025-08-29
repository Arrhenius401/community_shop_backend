package com.community_shop.backend.DTO.param;

import lombok.Data;

@Data
public class LoginParam {
    private String email;
    private String phoneNumber;
    private String password;
}
