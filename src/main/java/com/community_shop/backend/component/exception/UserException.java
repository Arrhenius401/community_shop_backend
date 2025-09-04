package com.community_shop.backend.component.exception;

import com.community_shop.backend.component.enums.errorcode.ErrorCode;

public class UserException extends BusinessException {
    public UserException(String code, String message) {
        super(code, message);
    }

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
