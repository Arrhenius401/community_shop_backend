package xyz.graygoo401.common.exception;

import xyz.graygoo401.common.exception.error.SystemErrorCode;

/**
 * 未登录异常（用户接口校验失败时抛出）
 */
public class UnLoginException extends BusinessException {
    public UnLoginException(String message) {
        super(SystemErrorCode.PERMISSION_UNAUTHORIZED, message);
    }

    public UnLoginException() {
        super(SystemErrorCode.PERMISSION_UNAUTHORIZED);
    }
}
