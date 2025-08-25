package com.community_shop.backend.DTO.request;

import lombok.Data;

@Data
public class LoginParam {
    private String email;
    private String phoneNumber;
    private String password;
}
