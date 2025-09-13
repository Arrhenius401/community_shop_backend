package com.community_shop.backend.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 签名工具类
 * 用于支付回调签名验证、接口签名生成等场景
 * 采用MD5加盐哈希算法保证数据完整性和防篡改
 */
@Slf4j
@Component
public class SignUtil {

    /**
     * 验证签名（通用方法）
     * @param obj 待验证的参数对象
     * @param secret 签名密钥（不同场景使用不同密钥，如支付回调、接口调用）
     * @return 签名是否有效
     */
    public static boolean verifySign(Object obj, String secret) {
        if (obj == null || !StringUtils.hasText(secret)) {
            log.error("签名验证失败：参数对象或密钥为空");
            return false;
        }

        try {
            // 1. 将对象转换为Map
            Map<String, Object> paramMap = objectToMap(obj);

            // 2. 提取参数中的签名
            String sign = (String) paramMap.get("sign");
            if (!StringUtils.hasText(sign)) {
                log.error("签名验证失败：参数中未包含sign字段");
                return false;
            }

            // 3. 移除Map中的sign字段（避免参与签名计算）
            paramMap.remove("sign");

            // 4. 生成待验证的签名
            String generateSign = generateSign(paramMap, secret);

            // 5. 比较签名（忽略大小写）
            boolean isValid = sign.equalsIgnoreCase(generateSign);
            if (!isValid) {
                log.error("签名验证失败：实际签名={}, 计算签名={}", sign, generateSign);
            }
            return isValid;
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 生成签名
     * @param paramMap 参数Map
     * @param secret 签名密钥
     * @return 生成的签名字符串（32位大写MD5）
     */
    public static String generateSign(Map<String, Object> paramMap, String secret) {
        if (paramMap == null || paramMap.isEmpty() || !StringUtils.hasText(secret)) {
            log.error("生成签名失败：参数为空或密钥为空");
            return null;
        }

        try {
            // 1. 对参数按key进行ASCII升序排序
            List<String> keyList = new ArrayList<>(paramMap.keySet());
            Collections.sort(keyList);

            // 2. 拼接参数键值对（key=value&key=value）
            StringBuilder paramStr = new StringBuilder();
            for (String key : keyList) {
                Object value = paramMap.get(key);
                // 跳过空值和sign字段
                if (value == null || "sign".equals(key)) {
                    continue;
                }
                // 将值转换为字符串（避免数字类型差异导致签名不一致）
                String valueStr = value.toString().trim();
                if (paramStr.length() > 0) {
                    paramStr.append("&");
                }
                paramStr.append(key).append("=").append(valueStr);
            }

            // 3. 拼接密钥（key=value&key=value&secret=xxx）
            paramStr.append("&secret=").append(secret);
            String signSource = paramStr.toString();

            // 4. 计算MD5并转为大写
            String sign = DigestUtils.md5DigestAsHex(signSource.getBytes(StandardCharsets.UTF_8))
                    .toUpperCase();

            log.debug("生成签名：原始串={}, 签名结果={}", signSource, sign);
            return sign;
        } catch (Exception e) {
            log.error("生成签名异常", e);
            return null;
        }
    }

    /**
     * 将对象转换为Map（支持基本类型、包装类、String等）
     * @param obj 待转换的对象
     * @return 转换后的Map
     */
    private static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }

        // 使用fastjson将对象转为Map（也可使用Jackson等其他JSON工具）
        String jsonStr = JSONObject.toJSONString(obj);
        return JSONObject.parseObject(jsonStr, Map.class);
    }

    /**
     * 生成随机密钥（用于系统配置，如支付回调密钥）
     * @param length 密钥长度（建议16-32位）
     * @return 随机密钥字符串
     */
    public static String generateRandomSecret(int length) {
        if (length <= 0 || length > 128) {
            throw new IllegalArgumentException("密钥长度必须在1-128之间");
        }

        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(base.length());
            sb.append(base.charAt(index));
        }
        return sb.toString();
    }
}
