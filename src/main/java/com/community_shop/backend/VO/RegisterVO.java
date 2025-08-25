package com.community_shop.backend.VO;

import lombok.Data;

@Data
public class RegisterVO {
    // 手机号（与邮箱二选一）
    private String phoneNumber;
    // 邮箱（与手机号二选一）
    private String email;
    // 密码（需BCrypt加密）
    private String password;
    // 验证码（手机号/邮箱验证码）
    private String verificationCode;
}
