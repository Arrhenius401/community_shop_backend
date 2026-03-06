package xyz.graygoo401.common.annotation;

import java.lang.annotation.*;

/**
 * 滑动窗口限流注解
 */
@Target({ElementType.METHOD}) // 仅作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented
public @interface RateLimiter {

    /**
     * 限流维度（全局/IP/用户）
     */
    LimitDimension dimension() default LimitDimension.GLOBAL;

    /**
     * 滑动窗口大小（单位：秒）
     */
    int windowSize() default 60;

    /**
     * 窗口内最大请求数（阈值）
     */
    int limit() default 100;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * Redis Key 前缀（用于区分不同接口）
     */
    String keyPrefix() default "rate_limit:";

    /**
     * 枚举：限流维度
     */
    enum LimitDimension {
        GLOBAL, // 全局维度
        IP,     // IP维度
        USER    // 用户维度
    }
}
