package xyz.graygoo401.common.exception;

import xyz.graygoo401.common.exception.error.SystemErrorCode;

/**
 * 权限不足异常（管理员接口校验失败时抛出）
 */
public class NoPermissionException extends BusinessException {
    public NoPermissionException(String message) {
        super(SystemErrorCode.PERMISSION_DENIED, message);
    }

    public NoPermissionException() {
        super(SystemErrorCode.PERMISSION_DENIED);
    }
}
