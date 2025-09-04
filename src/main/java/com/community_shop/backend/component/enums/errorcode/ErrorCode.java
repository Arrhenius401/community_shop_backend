package com.community_shop.backend.component.enums.errorcode;

public enum ErrorCode {
    // SUCCESS、PARAM_ERROR等是ErrorCode枚举类的实例，其类型即为ErrorCode本身
    // 必须枚举在开头
    // 通用错误
    SUCCESS("SYSTEM_000", "操作成功"),
    FAILURE("SYSTEM_001", "操作失败"),
    PARAM_ERROR("SYSTEM_002", "参数错误"),
    PARAM_NULL("SYSTEM_003", "参数为空"),
    UNAUTHORIZED("SYSTEM_004", "未认证"),
    FORBIDDEN("SYSTEM_005", "无权限"),
    NOT_FOUND("SYSTEM_006", "资源不存在"),

    // 业务错误
    // 用户模块
    USER_EXISTS("USER_001", "用户已存在"),
    EMAIL_EXISTS("USER_002", "邮箱已被注册"),
    PHONE_EXISTS("USER_003", "手机号已被注册"),
    USERNAME_EXISTS("USER_004", "用户名已存在"),

    EMAIL_NULL("USER_011", "邮箱为空"),
    PHONE_NULL("USER_012", "手机号为空"),
    USERNAME_NULL("USER_013", "用户名为空"),
    ROLE_NULL("USER_014", "用户角色为空"),
    USER_ID_NULL("USER_015", "用户ID为空"),
    PASSWORD_NULL("USER_016", "密码为空"),

    ROLE_ERROR("USER_021","用户角色参数错误"),
    STATUS_ERROR("USER_022","用户状态参数错误"),

    USER_NOT_EXISTS("USER_031", "用户不存在"),
    PASSWORD_WRONG("USER_032", "密码错误"),

    // 用户第三方绑定相关
    USER_THIRD_PARTY_NOT_BOUND("USER_041", "第三方账号未绑定"),
    USER_THIRD_PARTY_BIND_FAILED("USER_042", "第三方账号绑定失败"),
    USER_THIRD_PARTY_UNBIND_FAILED("USER_043", "第三方账号解绑失败"),
    USER_THIRD_PARTY_NOT_EXISTS("USER_044", "第三方账号绑定记录不存在"),

    // 帖子模块
    POST_NOT_EXISTS("POST_001", "帖子不存在"),
    POST_TITLE_NULL("POST_002", "帖子标题为空"),
    POST_CONTENT_NULL("POST_003", "帖子内容为空"),
    POST_ID_NULL("POST_004", "帖子ID为空"),
    POST_STATUS_ERROR("POST_005", "帖子状态参数错误"),

    // 帖子跟帖模块
    POST_FOLLOW_NOT_EXISTS("POST_FOLLOW_001", "跟帖不存在"),
    POST_FOLLOW_CONTENT_NULL("POST_FOLLOW_002", "跟帖内容为空"),
    POST_FOLLOW_ID_NULL("POST_FOLLOW_003", "跟帖ID为空"),
    POST_FOLLOW_STATUS_ERROR("POST_FOLLOW_004", "跟帖状态参数错误"),

    // 商品模块
    PRODUCT_NOT_EXISTS("PRODUCT_001", "商品不存在"),
    PRODUCT_TITLE_NULL("PRODUCT_002", "商品标题为空"),
    PRODUCT_PRICE_NULL("PRODUCT_003", "商品价格为空"),
    PRODUCT_ID_NULL("PRODUCT_004", "商品ID为空"),
    PRODUCT_STATUS_ERROR("PRODUCT_005", "商品状态参数错误"),
    PRODUCT_CONDITION_ERROR("PRODUCT_006", "商品成色参数错误"),

    // 订单模块
    ORDER_NOT_EXISTS("ORDER_001", "订单不存在"),
    ORDER_ID_NULL("ORDER_002", "订单ID为空"),
    ORDER_AMOUNT_NULL("ORDER_003", "订单金额为空"),
    ORDER_STATUS_ERROR("ORDER_004", "订单状态参数错误"),
    ORDER_BUYER_NULL("ORDER_005", "订单买家为空"),
    ORDER_SELLER_NULL("ORDER_006", "订单卖家为空"),

    // 评价模块
    EVALUATION_NOT_EXISTS("EVAL_001", "评价不存在"),
    EVALUATION_ID_NULL("EVAL_002", "评价ID为空"),
    EVALUATION_CONTENT_NULL("EVAL_003", "评价内容为空"),
    EVALUATION_SCORE_NULL("EVAL_004", "评价分数为空"),
    EVALUATION_ORDER_ID_NULL("EVAL_005", "评价订单ID为空"),
    EVALUATION_STATUS_ERROR("EVAL_006", "评价状态参数错误"),

    // 消息模块
    MESSAGE_NOT_EXISTS("MSG_001", "消息不存在"),
    MESSAGE_ID_NULL("MSG_002", "消息ID为空"),
    MESSAGE_CONTENT_NULL("MSG_003", "消息内容为空"),
    MESSAGE_RECEIVER_NULL("MSG_004", "消息接收者为空"),
    MESSAGE_TYPE_ERROR("MSG_005", "消息类型参数错误"),
    MESSAGE_STATUS_ERROR("MSG_006", "消息状态参数错误"),

    // QA模块
    QA_ID_NULL("QA_001", "QA记录ID为空"),
    QA_QUESTION_TEXT_NULL("QA_002", "QA问题内容为空"),
    QA_NOT_EXISTS("QA_003", "QA记录不存在"),

    // Log模块
    LOG_NOT_EXISTS("LOG_001", "日志记录不存在"),
    LOG_TARGET_ID_NULL("LOG_002", "日志目标ID为空"),
    LOG_TARGET_TYPE_NULL("LOG_003", "日志目标类型为空"),
    LOG_ACTION_TYPE_NULL("LOG_004", "日志操作类型为空"),
    LOG_TARGET_TYPE_INVALID("LOG_005", "日志目标类型参数错误"),
    LOG_ACTION_TYPE_INVALID("LOG_006", "日志操作类型参数错误"),

    // 邮件模块
    MAIL_SEND_FAILS("MAIL_001", "邮件发送失败"),
    MAIL_REDIS_BREAK("MAIL_002", "Redis异常"),
    MAIL_SEND_FAILS_TOO_MANY_TIMES("MAIL_003", "邮件发送失败次数过多"),
    MAIL_VERIFICATION_CODE_EXPIRED("MAIL_004", "邮件验证码已过期"),
    MAIL_VERIFICATION_CODE_INVALID("MAIL_005", "邮件验证码无效"),
    MAIL_VERIFICATION_CODE_NOT_EXISTS("MAIL_006", "邮件验证码不存在"),
    MAIL_VERIFICATION_CODE_NOT_MATCH("MAIL_007", "邮件验证码不匹配");

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

