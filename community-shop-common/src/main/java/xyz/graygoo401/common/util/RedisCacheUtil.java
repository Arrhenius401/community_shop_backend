package xyz.graygoo401.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.graygoo401.common.dto.CacheData;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RedisCacheUtil {
    private final StringRedisTemplate stringRedisTemplate;

    // 异步更新缓存的线程池（核心数根据服务器配置调整）
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 查询缓存（逻辑过期方案 + FastJSON 1.x）
     * @param key 缓存Key
     * @param typeReference 泛型类型引用（解决1.x泛型解析问题）
     * @param dbFallback 数据库查询函数
     * @param expireSeconds 逻辑过期时间（秒）
     * @return 目标数据
     */
    public <T> T queryWithLogicalExpire(
            String key,
            TypeReference<CacheData<T>> typeReference,
            Function<String, T> dbFallback,
            long expireSeconds) {
        // 1. 从Redis读取缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 缓存不存在：直接查库 + 存入缓存
        if (!StringUtils.hasText(json)) {
            T data = dbFallback.apply(key);
            setWithLogicalExpire(key, data, expireSeconds);
            return data;
        }

        // 3. 缓存存在：FastJSON 1.x 反序列化（关键：用TypeReference解决泛型）
        CacheData<T> cacheData = JSON.parseObject(json, typeReference);
        T data = cacheData.getData();
        LocalDateTime expireTime = cacheData.getExpireTime();

        // 4. 判断逻辑是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回旧数据
            return data;
        }

        // 5. 已过期：加互斥锁，异步更新缓存
        String lockKey = "lock:" + key;
        if (tryLock(lockKey)) {
            // 异步更新，不阻塞当前请求
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查数据库获取最新数据
                    T newData = dbFallback.apply(key);
                    // 更新缓存（重置逻辑过期时间）
                    setWithLogicalExpire(key, newData, expireSeconds);
                } finally {
                    // 无论更新成功与否，释放锁
                    unlock(lockKey);
                }
            });
        }

        // 6. 无论是否更新缓存，都返回旧数据（保证接口响应速度）
        return data;
    }

    /**
     * 存入缓存（FastJSON 1.x 序列化）
     */
    private <T> void setWithLogicalExpire(String key, T data, long expireSeconds) {
        CacheData<T> cacheData = new CacheData<>();
        cacheData.setData(data);
        cacheData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // FastJSON 1.x 序列化：对象转JSON字符串
        String json = JSON.toJSONString(cacheData);
        // Redis存入（不设置TTL，避免自动删除）
        stringRedisTemplate.opsForValue().set(key, json);
    }

    /**
     * 获取Redis互斥锁（SETNX）
     */
    private boolean tryLock(String lockKey) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放Redis互斥锁
     */
    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }
}