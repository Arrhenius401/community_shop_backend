package com.community_shop.backend.exception;

/**
 * 权限不足异常（管理员接口校验失败时抛出）
 */
public class NoPermissionException extends BusinessException {
    public NoPermissionException(String message) {
        super("403", message);
    }

    public NoPermissionException() {
        super("403", "权限不足");
    }
}
