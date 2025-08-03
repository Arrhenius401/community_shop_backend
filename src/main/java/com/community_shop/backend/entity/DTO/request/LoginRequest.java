package com.community_shop.backend.entity.DTO.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String phoneNumber;
    private String password;
}
