package xyz.graygoo401.common.config;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Spring Data Redis执行Lua脚本工具类
 */
@Component
public class RedisLuaScriptUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 定义Lua脚本（示例：扣减库存，库存不足返回0，成功返回1）
    private static final String DECR_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +
                    "local num = tonumber(ARGV[1]) " +
                    "if stock and stock >= num then " +
                    "    redis.call('decrby', KEYS[1], num) " +
                    "    return 1 " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 扣减库存（原子操作）
     * @param stockKey 库存键
     * @param deductNum 扣减数量
     * @return 1=成功，0=失败
     */
    public Long deductStock(String stockKey, int deductNum) {
        // 1. 构建RedisScript对象
        RedisScript<Long> redisScript = new DefaultRedisScript<>(
                DECR_STOCK_SCRIPT, // 脚本内容
                Long.class         // 脚本返回值类型
        );

        // 2. 构造参数（KEYS是List，ARGV是可变参数）
        List<String> keys = Collections.singletonList(stockKey);

        // 3. 执行脚本
        return redisTemplate.execute(
                redisScript,   // 脚本对象
                keys,          // KEYS数组
                String.valueOf(deductNum) // ARGV参数（需转为字符串，避免类型问题）
        );
    }
}
