package com.community_shop.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 验证码缓存工具类
 */
@Component
public class CodeCacheUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 缓存前缀：区分验证码业务（避免与其他Rediskey冲突）
    private static final String CODE_PHONE_KEY_PREFIX = "code:phone";
    private static final String CODE_EMAIL_KEY_PREFIX = "code:email";

    // 缓存有效期：5分钟
    private static final Duration CACHE_TTL_CODE = Duration.ofMinutes(5);

    /**
     * 缓存手机号验证码到Redis
     * @param phone 手机号
     * @param code 验证码
     */
    public void cachePhoneCode(String phone, String code) {
        cachePhoneCode(phone, code, CACHE_TTL_CODE.getSeconds());
    }

    /**
     * 缓存手机号验证码到Redis
     * @param phone 手机号
     * @param code 验证码
     * @param expireSeconds 过期时间（秒）
     */
    public void cachePhoneCode(String phone, String code, long expireSeconds) {
        String key = CODE_PHONE_KEY_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 从Redis查询手机号验证码
     * @param phone 手机号
     * @return 缓存的验证码（null表示已过期或未缓存）
     */
    public String getCachePhoneCode(String phone) {
        String key = CODE_PHONE_KEY_PREFIX + phone;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 校验通过后删除手机号验证码缓存（防止重复使用）
     * @param phone 手机号
     */
    public void deleteCachedPhoneCode(String phone) {
        String key = CODE_PHONE_KEY_PREFIX + phone;
        stringRedisTemplate.delete(key);
    }

    /**
     * 缓存邮箱验证码到Redis
     * @param email 邮箱
     * @param code 验证码
     */
    public void cacheEmailCode(String email, String code) {
        cacheEmailCode(email, code, CACHE_TTL_CODE.getSeconds());
    }

    /**
     * 缓存邮箱验证码到Redis
     * @param email 邮箱
     * @param code 验证码
     * @param expireSeconds 过期时间（秒）
     */
    public void cacheEmailCode(String email, String code, long expireSeconds) {
        String key = CODE_EMAIL_KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 从Redis查询邮箱验证码
     * @param email 邮箱
     * @return 缓存的验证码（null表示已过期或未缓存）
     */
    public String getCacheEmailCode(String email) {
        String key = CODE_EMAIL_KEY_PREFIX + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 校验通过后删除邮箱验证码缓存（防止重复使用）
     * @param email 邮箱
     */
    public void deleteCachedEmailCode(String email) {
        String key = CODE_EMAIL_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
    }
}
