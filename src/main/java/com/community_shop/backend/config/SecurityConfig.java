package com.community_shop.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** 注册BCrypt密码编码器
     * 在 BCrypt 算法中，盐值的生成、使用和存储是全自动的，无需手动管理
     * 在用户输入原始密码时，自动生成的随机盐值与原始密码混合后，通过哈希算法计算出加密结果。最终存储到数据库的字符串中，已包含盐值（无需单独建字段存储盐值）。
     * 用户输入密码时，后端从数据库取出该用户的加密密码（如$2a$10$N9qo8uLOickgx2ZMRZo5MeVQ82i0t8Q13W9XQ60hDqgX8Fy13u5q）；
     * BCrypt 自动从加密字符串中提取出当初的随机盐值（N9qo8uLOickgx2ZMRZo5MeVQ）。最后用提取的盐值与用户输入的密码重新计算哈希，对比是否与数据库中的加密结果一致。
     * @return BCrypt密码编码器
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 参数strength：加密强度（4-31之间，默认10），值越大加密越慢但安全性越高
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 配置安全过滤链
     * 仅负责基础安全配置，将认证逻辑完全交给自定义拦截器
     * @param http HttpSecurity对象
     * @return SecurityFilterChain对象
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF（开发环境）
                .csrf(csrf -> csrf.disable())
                // 2. 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        // 放行所有请求，由AuthInterceptor处理权限校验
                        .anyRequest().permitAll()
                )
                // 3. 保留表单登录（可选，不影响 Swagger 访问）
                .formLogin(form -> form.permitAll())
                // 4. 关键：排除 SpringDoc 接口的 Security 过滤器（避免拦截）
                .securityMatcher("/api/**") // 仅对 /api/** 路径应用 Security 规则
                .httpBasic(httpBasic -> {});

        return http.build();
    }
}
