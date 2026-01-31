package xyz.graygoo401.common.exception.error;

/**
 * 通用业务错误码接口
 */
public interface IErrorCode {
    String getCode();         // 业务错误码 (如 USER_001)
    int getStandardCode();    // HTTP 状态码 (如 400)
    String getMessage();      // 错误信息
}
