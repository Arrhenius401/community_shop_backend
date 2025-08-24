package com.community_shop.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // 注册BCrypt密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 参数strength：加密强度（4-31之间，默认10），值越大加密越慢但安全性越高
        return new BCryptPasswordEncoder(12);
    }
}
