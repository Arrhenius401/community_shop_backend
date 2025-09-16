package com.community_shop.backend.enums.ErrorCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // SUCCESS、PARAM_ERROR等是ErrorCode枚举类的实例，其类型即为ErrorCode本身
    // 必须枚举在开头
    // 通用错误
    SUCCESS("SYSTEM_000", "操作成功"),
    FAILURE("SYSTEM_001", "操作失败"),
    PARAM_ERROR("SYSTEM_002", "参数错误"),
    PARAM_NULL("SYSTEM_003", "参数为空"),
    NOT_FOUND("SYSTEM_004", "资源不存在"),
    RELATED_DATA_MISSING("SYSTEM_005", "缺少关联数据"),

    DATA_UPDATE_FAILED("SYSTEM_011", "数据更新失败"),
    DATA_DELETE_FAILED("SYSTEM_012", "数据删除失败"),
    DATA_INSERT_FAILED("SYSTEM_013", "数据插入失败"),
    DATA_QUERY_FAILED("SYSTEM_014", "数据查询失败"),

    PERMISSION_UNAUTHORIZED("SYSTEM_021", "未认证"),
    PERMISSION_DENIED("SYSTEM_022", "无权限"),

    OPERATION_REPEAT("SYSTEM_051", "操作重复"),

    SYSTEM_ERROR("SYSTEM_100", "系统错误"),

    // 业务错误
    // 用户模块
    USER_EXISTS("USER_001", "用户已存在"),
    EMAIL_EXISTS("USER_002", "邮箱已被注册"),
    PHONE_EXISTS("USER_003", "手机号已被注册"),
    USERNAME_EXISTS("USER_004", "用户名已存在"),

    PASSWORD_CONFIRM_NOT_MATCH("USER_005", "密码确认不一致"),
    OLD_PASSWORD_ERROR("USER_006", "旧密码错误"),

    EMAIL_NULL("USER_011", "邮箱为空"),
    PHONE_NULL("USER_012", "手机号为空"),
    USERNAME_NULL("USER_013", "用户名为空"),
    ROLE_NULL("USER_014", "用户角色为空"),
    USER_ID_NULL("USER_015", "用户ID为空"),
    PASSWORD_NULL("USER_016", "密码为空"),

    ROLE_INVALID("USER_021","用户角色参数错误"),
    STATUS_INVALID("USER_022","用户状态参数错误"),
    USERNAME_FORMAT_INVALID("USER_023", "用户名格式错误"),
    PHONE_FORMAT_INVALID("USER_024", "手机号格式错误"),
    EMAIL_FORMAT_INVALID("USER_025", "邮箱格式错误"),
    PASSWORD_FORMAT_INVALID("USER_027", "密码格式错误"),
    USERNAME_LENGTH_INVALID("USER_028", "用户名长度错误"),
    AVATAR_URL_FORMAT_INVALID("USER_029", "头像URL格式错误"),
    VERIFY_CODE_INVALID("USER_030", "验证码错误"),


    USER_NOT_EXISTS("USER_051", "用户不存在"),
    PASSWORD_ERROR("USER_052", "密码错误"),


    CREDIT_TOO_LOW("USER_081", "用户积分不足"),

    // 用户第三方绑定相关
    THIRD_AUTH_FAILED("USER_071", "第三方授权失败"),
    THIRD_SYSTEM_ERROR("USER_072", "第三方系统错误"),
    USER_THIRD_PARTY_UNBIND_FAILED("USER_073", "第三方账号解绑失败"),
    USER_THIRD_PARTY_NOT_EXISTS("USER_074", "第三方账号绑定记录不存在"),
    USER_THIRD_PARTY_NOT_BOUND("USER_075", "第三方账号未绑定"),
    USER_THIRD_PARTY_BIND_FAILED("USER_076", "第三方账号绑定失败"),

    // 帖子模块
    POST_NOT_EXISTS("POST_001", "帖子不存在"),
    POST_TITLE_NULL("POST_002", "帖子标题为空"),
    POST_CONTENT_NULL("POST_003", "帖子内容为空"),
    POST_ID_NULL("POST_004", "帖子ID为空"),
    POST_STATUS_ERROR("POST_005", "帖子状态参数错误"),

    POST_STATUS_INVALID("POST_021", "帖子状态参数错误"),
    POST_TITLE_INVALID("POST_022", "帖子标题不符合规范"),
    POST_CONTENT_INVALID("POST_023", "帖子内容不符合规范"),
    POST_IMAGE_FORMAT_INVALID("POST_024", "帖子图片格式错误"),

    POST_IMAGE_TOO_MANY("POST_031", "帖子图片数量过多"),

    POST_ALREADY_DELETED("POST_051", "帖子已删除"),

    TOP_POST_COUNT_EXCEED("POST_071", "帖子置顶数量超出限制"),
    DAILY_LIKE_TIMES_EXCEED("POST_072", "用户每天点赞次数超出限制"),

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
    PRODUCT_CATEGORY_NULL("PRODUCT_005", "商品分类为空"),

    PRODUCT_TITLE_INVALID("PRODUCT_011", "商品标题不符合规范"),
    PRODUCT_STATUS_INVALID("PRODUCT_012", "商品状态参数错误"),
    PRODUCT_CONDITION_INVALID("PRODUCT_013", "商品成色参数错误"),
    PRODUCT_PRICE_INVALID("PRODUCT_014", "商品价格参数错误"),
    PRODUCT_STOCK_INVALID("PRODUCT_015", "商品库存参数错误"),
    PRODUCT_IMAGE_URL_INVALID("PRODUCT_016", "商品图片URL参数错误"),

    PRODUCT_DESCRIPTION_TOO_LONG("PRODUCT_021", "商品描述过长"),
    PRODUCT_IMAGE_TOO_MANY("PRODUCT_022", "商品图片数量过多"),

    PRODUCT_ALREADY_OFF_SALE("PRODUCT_091", "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT("PRODUCT_092", "商品库存不足"),
    ORDER_AMOUNT_ABNORMAL("ORDER_021", "订单金额异常"),



    // 订单模块
    ORDER_NOT_EXISTS("ORDER_001", "订单不存在"),
    ORDER_ID_NULL("ORDER_002", "订单ID为空"),
    ORDER_AMOUNT_NULL("ORDER_003", "订单金额为空"),
    ORDER_STATUS_NULL("ORDER_004", "订单状态为空"),
    ORDER_BUYER_NULL("ORDER_005", "订单买家为空"),
    ORDER_SELLER_NULL("ORDER_006", "订单卖家为空"),

    ORDER_STATUS_INVALID("ORDER_004", "订单状态参数错误"),

    STOCK_INSUFFICIENT("ORDER_021", "商品库存不足"),

    ORDER_STATUS_NOT_COMPLETED("ORDER_005", "订单未完成"),


    // 评价模块
    EVALUATION_NOT_EXISTS("EVAL_001", "评价不存在"),
    EVALUATION_ID_NULL("EVAL_002", "评价ID为空"),
    EVALUATION_CONTENT_NULL("EVAL_003", "评价内容为空"),
    EVALUATION_SCORE_NULL("EVAL_004", "评价分数为空"),
    EVALUATION_ORDER_ID_NULL("EVAL_005", "评价订单ID为空"),

    EVALUATION_REPORT_REASON_NULL("EVAL_006", "评价举报理由为空"),


    EVALUATION_SCORE_INVALID("EVAL_021", "评价分数参数错误"),
    EVALUATION_CONTENT_INVALID("EVAL_022", "评价内容参数错误"),
    EVALUATION_IMAGE_URL_INVALID("EVAL_023", "评价图片URL参数错误"),
    EVALUATION_TAGS_INVALID("EVAL_024", "评价标签参数错误"),
    EVALUATION_STATUS_INVALID("EVAL_025", "评价状态参数错误"),

    EVALUATION_REPORT_REASON_TOO_LONG("EVAL_041", "评价举报理由过长"),


    ORDER_ALREADY_EVALUATED("EVAL_091", "订单已评价"),
    SELLER_NOT_EXISTS("MSG_002", "卖家不存在"),


    // 消息模块
    MESSAGE_NOT_EXISTS("MSG_001", "消息不存在"),
    MESSAGE_ID_NULL("MSG_002", "消息ID为空"),
    MESSAGE_CONTENT_NULL("MSG_003", "消息内容为空"),
    MESSAGE_RECEIVER_NULL("MSG_004", "消息接收者为空"),
    MESSAGE_SENDER_NULL("MSG_005", "消息发送者为空"),
    MESSAGE_TYPE_NULL("MSG_006", "消息类型为空"),


    MESSAGE_TYPE_INVALID("MSG_005", "消息类型参数错误"),
    MESSAGE_STATUS_INVALID("MSG_006", "消息状态参数错误"),
    MESSAGE_CONTENT_INVALID("MSG_007", "消息内容参数错误"),

    RECEIVER_NOT_EXISTS("MSG_011", "接收者不存在"),
    SENDER_NOT_EXISTS("MSG_012", "发送者不存在"),


    MESSAGE_STATUS_TRANSITION_INVALID("MSG_004", "消息状态转换错误"),

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
}

