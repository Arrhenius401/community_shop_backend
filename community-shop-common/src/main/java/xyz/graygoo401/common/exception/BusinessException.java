package xyz.graygoo401.common.exception;

import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;
import xyz.graygoo401.common.exception.error.SystemErrorCode;

/**
 * 通用业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    private final IErrorCode errorCode;

    // 1. 无参构造（原有的）
    public BusinessException() {
        super(SystemErrorCode.FAILURE.getMessage());
        this.errorCode = SystemErrorCode.FAILURE;
    }

    // 2. 单参数：仅错误码
    public BusinessException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 3. 单参数：仅自定义消息
    public BusinessException(String message) {
        super(message);
        this.errorCode = SystemErrorCode.FAILURE;
    }

    // 4. 双参数：错误码 + 自定义消息
    public BusinessException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
