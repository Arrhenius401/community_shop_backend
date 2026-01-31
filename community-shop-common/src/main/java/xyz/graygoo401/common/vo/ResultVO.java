package xyz.graygoo401.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.common.exception.error.IErrorCode;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * @param <T> 业务数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果封装")
public class ResultVO<T> implements Serializable {
    /** 状态码：200=成功，其他为错误码（如400=参数错误、500=系统异常） */
    @Schema(description = "状态码（200=成功，其他为错误码）", example = "200")
    private String code;

    /** 返回消息：成功时为"success"，失败时为具体错误描述 */
    @Schema(description = "响应消息", example = "success")
    private String message;

    /** 返回数据：泛型类型，存储业务数据（如用户信息、订单列表等） */
    @Schema(description = "业务数据")
    private T data;

    /** 响应时间戳（毫秒级） */
    @Schema(description = "响应时间戳（毫秒）", example = "1695678901234")
    private Long timestamp;

    /**
     * 静态方法，用于创建成功的响应结果
     * @param <T> 业务数据泛型
     * @return 响应结果对象
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>("200", "success", null, System.currentTimeMillis());
    }

    /**
     * 静态方法，用于创建成功的响应结果，并携带业务数据
     * @param <T> 业务数据泛型
     * @param data 业务数据
     * @return 响应结果对象
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>("200", "success", data, System.currentTimeMillis());
    }

    /**
     * 静态方法，用于创建失败的响应结果
     * @param <T> 业务数据泛型
     * @param message 错误信息
     * @return 响应结果对象
     */
    public static <T> ResultVO<T> fail(String message) {
        return new ResultVO<>("500", message, null, System.currentTimeMillis());
    }

    /**
     * 静态方法，用于创建失败的响应结果，并指定错误码和错误信息
     * @param <T> 业务数据泛型
     * @param code 错误码
     * @param message 错误信息
     * @return 响应结果对象
     */
    public static <T> ResultVO<T> fail(String code, String message) {
        return new ResultVO<>(code, message, null, System.currentTimeMillis());
    }

    /**
     * 静态方法，用于创建失败的响应结果，并指定错误码和错误信息
     * @param <T> 业务数据泛型
     * @param errorCode 错误码枚举
     * @return 响应结果对象
     */
    public static <T> ResultVO<T> fail(IErrorCode errorCode) {
        return new ResultVO<>(Integer.toString(errorCode.getStandardCode()), errorCode.getMessage(), null, System.currentTimeMillis());
    }

    public static <T> ResultVO<T> fail(IErrorCode errorCode, String message) {
        return new ResultVO<>(Integer.toString(errorCode.getStandardCode()), message, null, System.currentTimeMillis());
    }

}