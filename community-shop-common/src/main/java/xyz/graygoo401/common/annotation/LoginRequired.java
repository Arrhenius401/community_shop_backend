package xyz.graygoo401.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录校验注解（标注在Controller方法上，需登录才能访问）
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE}) //  新增 ANNOTATION_TYPE，允许被其他注解和方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface LoginRequired {
}
