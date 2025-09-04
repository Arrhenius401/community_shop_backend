package com.community_shop.backend.component.enums.errorcode;

public enum ErrorCode {
    // SUCCESS、PARAM_ERROR等是ErrorCode枚举类的实例，其类型即为ErrorCode本身
    // 必须枚举在开头
    // 通用错误
    SUCCESS("A000", "操作成功"),
    FAILURE("A001", "操作失败"),
    PARAM_ERROR("A002", "参数错误"),
    UNAUTHORIZED("A003", "未认证"),
    FORBIDDEN("A004", "无权限"),
    NOT_FOUND("A005", "资源不存在"),

    // 业务错误
    // 用户模块
    EMAIL_EXISTS("U001", "邮箱已被注册"),
    PHONE_EXISTS("U002", "手机号已被注册"),
    USERNAME_EXISTS("U003", "用户名已存在"),

    EMAIL_NULL("U011", "邮箱为空"),
    PHONE_NULL("U012", "手机号为空"),
    USERNAME_NULL("U013", "用户名为空"),
    ROLE_NULL("U014", "用户角色为空"),
    USER_ID_NULL("U015", "用户ID为空"),
    PASSWORD_NULL("U016", "密码为空"),

    ROLE_ERROR("U021","用户角色参数错误"),
    STATUS_ERROR("U022","用户状态参数错误"),

    USER_NOT_EXISTS("U031", "用户不存在"),
    PASSWORD_WRONG("U032", "密码错误"),

    // QA模块
    QA_ID_NULL("Q001", "QA记录ID为空"),
    QA_QUESTION_TEXT_NULL("Q002", "QA问题内容为空"),
    QA_NOT_EXISTS("Q003", "QA记录不存在"),

    // Log模块
    LOG_NOT_EXISTS("L001", "日志记录不存在"),
    LOG_TARGET_ID_NULL("L002", "日志目标ID为空"),
    LOG_TARGET_TYPE_NULL("L003", "日志目标类型为空"),
    LOG_ACTION_TYPE_NULL("L004", "日志操作类型为空"),
    LOG_TARGET_TYPE_INVALID("L005", "日志目标类型参数错误"),
    LOG_ACTION_TYPE_INVALID("L006", "日志操作类型参数错误"),

    // 邮件模块
    MAIL_SEND_FAILS("M001", "邮件发送失败"),
    MAIL_REDIS_BREAK("M002", "Redis异常"),
    MAIL_SEND_FAILS_TOO_MANY_TIMES("M003", "邮件发送失败次数过多"),
    MAIL_VERIFICATION_CODE_EXPIRED("M004", "邮件验证码已过期"),
    MAIL_VERIFICATION_CODE_INVALID("M005", "邮件验证码无效"),
    MAIL_VERIFICATION_CODE_NOT_EXISTS("M006", "邮件验证码不存在"),
    MAIL_VERIFICATION_CODE_NOT_MATCH("M007", "邮件验证码不匹配");

    private final String code;     // 错误码
    private final String message;   // 错误信息

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

