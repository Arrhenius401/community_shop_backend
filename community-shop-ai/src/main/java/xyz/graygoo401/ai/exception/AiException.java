package xyz.graygoo401.ai.exception;

import xyz.graygoo401.ai.exception.error.AiErrorCode;
import xyz.graygoo401.common.exception.BusinessException;

/**
 * AI服务异常
 */
public class AiException extends BusinessException {

    public AiException(AiErrorCode errorCode) {
        super(errorCode);
    }

    public AiException(String message) {
        super(AiErrorCode.AI_SERVICE_FAILS, message);
    }

    public AiException(AiErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AiException() {
        super(AiErrorCode.AI_SERVICE_FAILS);
    }
}
