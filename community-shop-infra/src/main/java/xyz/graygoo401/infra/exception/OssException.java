package xyz.graygoo401.infra.exception;

import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.exception.error.IErrorCode;
import xyz.graygoo401.infra.exception.error.OssErrorCode;

/**
 * OSS 对象存储服务异常类
 */
public class OssException extends BusinessException {
    // 1. 无参构造（原有的）
    public OssException()  {
        super(OssErrorCode.OSS_SERVICE_FAILS);
    }

    // 2. 单参数：仅错误码
    public OssException(IErrorCode errorCode) {
        super(errorCode); // 调用父类 BusinessException(IErrorCode) 构造方法
    }

    // 3. 单参数：仅自定义消息（若需要）
    public OssException(String message) {
        super(OssErrorCode.OSS_SERVICE_FAILS, message); // 调用父类双参数构造
    }

    // 4. 双参数：错误码 + 自定义消息
    public OssException(IErrorCode errorCode, String message) {
        super(errorCode, message); // 调用父类 BusinessException(IErrorCode, String) 构造方法
    }
}
