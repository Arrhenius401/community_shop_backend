package com.community_shop.backend.exception;

import com.community_shop.backend.exception.error.ErrorCode;

public class UnLoginException extends BusinessException {
    public UnLoginException(String message) {
        super(ErrorCode.PERMISSION_UNAUTHORIZED, message);
    }

    public UnLoginException() {
        super(ErrorCode.PERMISSION_UNAUTHORIZED);
    }
}
