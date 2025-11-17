package com.community_shop.backend.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 验证码生成工具类
 */
@Component
public class CodeGenerateUtil {

    /**
     * 生成6位随机数字验证码
     */
    public String generate6DigitCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10)); // 0-9的随机数
        }
        return code.toString();
    }
}
