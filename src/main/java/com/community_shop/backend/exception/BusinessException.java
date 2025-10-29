package com.community_shop.backend.exception;

import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final Integer standardCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.standardCode = errorCode.getStandardCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.standardCode = errorCode.getStandardCode();
    }
}
