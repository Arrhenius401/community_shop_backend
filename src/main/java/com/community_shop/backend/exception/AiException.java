package com.community_shop.backend.exception;

import com.community_shop.backend.exception.error.ErrorCode;

/**
 * AI服务异常
 */
public class AiException extends BusinessException {

    public AiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AiException(String message) {
        super(ErrorCode.AI_SERVICE_FAILS, message);
    }

    public AiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AiException() {
        super(ErrorCode.AI_SERVICE_FAILS);
    }
}
