package com.community_shop.backend.exception;

public class UnLoginException extends BusinessException {
    public UnLoginException(String message) {
        super("401", message);
    }

    public UnLoginException() {
        super("401", "用户未登录");
    }
}
