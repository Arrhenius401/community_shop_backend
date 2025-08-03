package com.community_shop.backend.component.exception;

import com.community_shop.backend.component.exception.errorcode.ErrorCode;

public class BaseException extends RuntimeException{
    //错误码
    private final int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(ErrorCode errorCode) {
        // 使用枚举错误码
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
