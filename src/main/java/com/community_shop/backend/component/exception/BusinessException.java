package com.community_shop.backend.component.exception;

import com.community_shop.backend.component.exception.errorcode.ErrorCode;

public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public String getCode() {
        return code;
    }
}
