package com.community_shop.backend.component.exception;

import com.community_shop.backend.component.exception.errorcode.ErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
