package com.community_shop.backend.service.exception;

public class BaseException extends RuntimeException{
    //错误码
    private int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }
}
