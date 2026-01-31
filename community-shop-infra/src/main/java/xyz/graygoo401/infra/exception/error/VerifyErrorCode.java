package xyz.graygoo401.infra.exception.error;

import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 验证码模块错误码枚举类
 */
@Getter
public enum VerifyErrorCode implements IErrorCode {

    VERIFY_CODE_INVALID("VERIFY_001", 400, "验证码服务出错"),
    VERIFY_CODE_SEND_FAILED("VERIFY_002", 500, "验证码发送失败"),
    VERIFY_CODE_VERIFY_FAILED("VERIFY_003", 400, "验证码验证失败"),
    VERIFY_CODE_NOT_EXISTS("VERIFY_004", 404, "验证码不存在"),
    VERIFY_CODE_EXPIRED("VERIFY_005", 400, "验证码已过期"),
    VERIFY_CODE_NOT_MATCH("VERIFY_006", 400, "验证码不匹配");

    private final String code;
    private final int standardCode;
    private final String message;

    VerifyErrorCode(String code, int standardCode, String message) {
        this.code = code;
        this.standardCode = standardCode;
        this.message = message;
    }
}
