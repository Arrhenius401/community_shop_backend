package xyz.graygoo401.user.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 第三方服务错误码
 */
@AllArgsConstructor
@Getter
public enum UserThirdPartyErrorCode implements IErrorCode {

    // 用户第三方绑定相关
    THIRD_AUTH_FAILED("USER_071", 401, "第三方授权失败"),
    THIRD_SYSTEM_ERROR("USER_072", 500, "第三方系统错误"),
    USER_THIRD_PARTY_UNBIND_FAILED("USER_073", 500, "第三方账号解绑失败"),
    USER_THIRD_PARTY_NOT_EXISTS("USER_074", 404, "第三方账号绑定记录不存在"),
    USER_THIRD_PARTY_NOT_BOUND("USER_075", 400, "第三方账号未绑定"),
    USER_THIRD_PARTY_BIND_FAILED("USER_076", 500, "第三方账号绑定失败");

    private final String code;
    private final int standardCode;
    private final String message;

}
