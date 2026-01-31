package xyz.graygoo401.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员校验注解（自动包含登录校验，标注在Controller方法上）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@LoginRequired // 继承登录校验，无需重复标注@LoginRequired
public @interface AdminRequired {
}
