package com.community_shop.backend.vo;

import com.community_shop.backend.enums.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * @param <T> 业务数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVO<T> implements Serializable {
    // 状态码：200=成功，其他为错误码（如400=参数错误、500=系统异常）
    private String code;
    // 返回消息：成功时为"success"，失败时为具体错误描述
    private String message;
    // 返回数据：泛型类型，存储业务数据（如用户信息、订单列表等）
    private T data;
    // 响应时间戳（毫秒级）
    private Long timestamp;


    // 判断是否成功只要判断code是否是200
    // 成功响应静态方法（无数据）
    public static <T> ResultVO<T> success() {
        return new ResultVO<>("200", "success", null, System.currentTimeMillis());
    }

    // 成功响应静态方法（带数据）
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>("200", "success", data, System.currentTimeMillis());
    }

    // 失败响应静态方法（带错误信息）
    public static <T> ResultVO<T> fail(String message) {
        return new ResultVO<>("500", message, null, System.currentTimeMillis());
    }

    // 失败响应静态方法（带自定义错误码和错误信息）
    public static <T> ResultVO<T> fail(String code, String message) {
        return new ResultVO<>(code, message, null, System.currentTimeMillis());
    }

    // 失败响应静态方法（使用ErrorCode枚举类）
    public static <T> ResultVO<T> fail(ErrorCode errorCode) {
        return new ResultVO<>(errorCode.getCode(), errorCode.getMessage(), null, System.currentTimeMillis());
    }

}
