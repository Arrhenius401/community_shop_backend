package xyz.graygoo401.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.graygoo401.common.annotation.RateLimiter;
import xyz.graygoo401.common.exception.RateLimitException;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面类
 */
@Aspect
@Component
public class RateLimiterAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 切入点：拦截所有标注@RateLimiter的方法
    @Pointcut("@annotation(xyz.graygoo401.common.annotation.RateLimiter)")
    public void rateLimitPointcut() {}

    /**
     * 环绕通知：执行限流逻辑
     */
    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);

        // 2. 构建Redis Key（根据维度区分）
        String redisKey = buildRedisKey(rateLimiter, method);
        int windowSize = rateLimiter.windowSize();
        int limit = rateLimiter.limit();
        String message = rateLimiter.message();

        // 3. 滑动窗口限流核心逻辑
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        long currentTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString(); // 唯一请求ID

        // 步骤1：清理窗口外的过期数据（滑动窗口核心）
        zSetOps.removeRangeByScore(redisKey, 0, currentTime - windowSize * 1000L);

        // 步骤2：统计当前窗口内的请求数
        Long currentCount = zSetOps.zCard(redisKey);

        // 步骤3：判断是否超过阈值
        if (currentCount != null && currentCount >= limit) {
            throw new RateLimitException(message);
        }

        // 步骤4：将当前请求加入ZSet（score为时间戳，value为唯一ID）
        zSetOps.add(redisKey, requestId, currentTime);

        // 步骤5：设置Redis Key过期时间（避免内存泄漏，窗口大小+1秒）
        redisTemplate.expire(redisKey, windowSize + 1, TimeUnit.SECONDS);

        // 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 构建Redis Key（多维度区分）
     */
    private String buildRedisKey(RateLimiter rateLimiter, Method method) {
        // 基础Key：前缀 + 类名 + 方法名（区分不同接口）
        String baseKey = rateLimiter.keyPrefix() + method.getDeclaringClass().getName() + ":" + method.getName();

        // 根据维度拼接Key
        RateLimiter.LimitDimension dimension = rateLimiter.dimension();
        switch (dimension) {
            case IP:
                // 获取请求IP
                String ip = getRequestIp();
                return baseKey + ":ip:" + ip;
            case USER:
                // 获取当前用户ID（需根据你的业务调整，比如从Token/上下文获取）
                String userId = getCurrentUserId();
                if (StringUtils.isBlank(userId)) {
                    throw new RateLimitException("用户未登录，无法按用户维度限流");
                }
                return baseKey + ":user:" + userId;
            case GLOBAL:
            default:
                // 全局维度：直接使用基础Key
                return baseKey + ":global";
        }
    }

    /**
     * 获取请求IP（适配代理场景）
     */
    private String getRequestIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多IP场景取第一个
        if (StringUtils.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前用户ID（需根据你的业务实现，这里仅示例）
     */
    private String getCurrentUserId() {
        // 示例：从请求头获取Token，解析出用户ID；或从ThreadLocal获取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader("UserId"); // 实际业务中替换为Token解析逻辑
    }
}