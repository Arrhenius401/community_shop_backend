package com.community_shop.backend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncoder() {
        for (int i = 0; i < 10; i++) {
            // 对明文"112233"进行加密
            String encodedPassword = passwordEncoder.encode("112233");
            // 输出加密后的密文
            System.out.println("密文：" + encodedPassword);
        }

    }
}
