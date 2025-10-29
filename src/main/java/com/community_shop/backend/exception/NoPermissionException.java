package com.community_shop.backend.exception;

import com.community_shop.backend.enums.ErrorCode.ErrorCode;

/**
 * 权限不足异常（管理员接口校验失败时抛出）
 */
public class NoPermissionException extends BusinessException {
    public NoPermissionException(String message) {
        super(ErrorCode.PERMISSION_DENIED, message);
    }

    public NoPermissionException() {
        super(ErrorCode.PERMISSION_DENIED);
    }
}
