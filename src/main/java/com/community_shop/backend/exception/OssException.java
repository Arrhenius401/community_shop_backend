package com.community_shop.backend.exception;

import com.community_shop.backend.exception.error.ErrorCode;

/**
 * OSS 对象存储服务异常类
 */
public class OssException extends BusinessException {
    public OssException(String message) {
        super(ErrorCode.OSS_SERVICE_FAILS, message);
    }

    public OssException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OssException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public OssException()  {
        super(ErrorCode.OSS_SERVICE_FAILS);
    }
}
