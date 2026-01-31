package xyz.graygoo401.user.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 用户模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum UserErrorCode implements IErrorCode {

    USER_EXISTS("USER_001", 409, "用户已存在"),
    EMAIL_EXISTS("USER_002", 409, "邮箱已被注册"),
    PHONE_EXISTS("USER_003", 409, "手机号已被注册"),
    USERNAME_EXISTS("USER_004", 409, "用户名已存在"),

    PASSWORD_CONFIRM_NOT_MATCH("USER_005", 400, "密码确认不一致"),
    OLD_PASSWORD_ERROR("USER_006", 400, "旧密码错误"),

    EMAIL_NULL("USER_011", 400, "邮箱为空"),
    PHONE_NULL("USER_012", 400, "手机号为空"),
    USERNAME_NULL("USER_013", 400, "用户名为空"),
    ROLE_NULL("USER_014", 400, "用户角色为空"),
    USER_ID_NULL("USER_015", 400, "用户ID为空"),
    PASSWORD_NULL("USER_016", 400, "密码为空"),

    ROLE_INVALID("USER_021", 400, "用户角色参数错误"),
    STATUS_INVALID("USER_022", 400, "用户状态参数错误"),
    USERNAME_FORMAT_INVALID("USER_023", 400, "用户名格式错误"),
    PHONE_FORMAT_INVALID("USER_024", 400, "手机号格式错误"),
    EMAIL_FORMAT_INVALID("USER_025", 400, "邮箱格式错误"),
    PASSWORD_FORMAT_INVALID("USER_027", 400, "密码格式错误"),
    USERNAME_LENGTH_INVALID("USER_028", 400, "用户名长度错误"),
    AVATAR_URL_FORMAT_INVALID("USER_029", 400, "头像URL格式错误"),

    USER_NOT_EXISTS("USER_051", 404, "用户不存在"),
    PASSWORD_ERROR("USER_052", 401, "密码错误"),

    CREDIT_TOO_LOW("USER_081", 400, "用户积分不足"),

    USER_NOT_MATCH("USER_101", 400, "用户不匹配"),

    EMAIL_NOT_BELONG_TO_USER("USER_201", 400, "邮箱不属于当前用户"),
    PHONE_NOT_BELONG_TO_USER("USER_202", 400, "手机号不属于当前用户"),

    VERIFY_FAILURE("USER_301", 400, "验证码验证失败");

    private final String code;
    private final int standardCode;
    private final String message;

}
