package com.community_shop.backend.DTO.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果DTO，封装所有Service层方法的返回数据
 * 适配《社区交易系统 Service 层设计文档》中各方法的返回值要求
 * @param <T> 泛型参数，支持返回单一数据、分页结果等多种类型
 */
@Data
public class ResultDTO<T> implements Serializable {
    // 状态码：200=成功，其他为错误码（如400=参数错误、500=系统异常）
    private String code;
    // 返回消息：成功时为"success"，失败时为具体错误描述
    private String message;
    // 返回数据：泛型类型，存储业务数据（如用户信息、订单列表等）
    private T data;

    // 无参构造器
    public ResultDTO() {}

    // 全参构造器
    public ResultDTO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 判断是否成功只要判断code是否是200
    // 成功响应静态方法（无数据）
    public static <T> ResultDTO<T> success() {
        return new ResultDTO<>("200", "success", null);
    }

    // 成功响应静态方法（带数据）
    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>("200", "success", data);
    }

    // 失败响应静态方法（带错误信息）
    public static <T> ResultDTO<T> fail(String message) {
        return new ResultDTO<>("500", message, null);
    }

    // 失败响应静态方法（带自定义错误码和错误信息）
    public static <T> ResultDTO<T> fail(String code, String message) {
        return new ResultDTO<>(code, message, null);
    }

}
