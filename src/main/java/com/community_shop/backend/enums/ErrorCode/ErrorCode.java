package com.community_shop.backend.enums.ErrorCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误
    SUCCESS("SYSTEM_000", 200, "操作成功"),
    FAILURE("SYSTEM_001", 500, "操作失败"),
    PARAM_ERROR("SYSTEM_002", 400, "参数错误"),
    PARAM_NULL("SYSTEM_003", 400, "参数为空"),
    NOT_FOUND("SYSTEM_004", 404, "资源不存在"),
    RELATED_DATA_MISSING("SYSTEM_005", 400, "缺少关联数据"),

    DATA_UPDATE_FAILED("SYSTEM_011", 500, "数据更新失败"),
    DATA_DELETE_FAILED("SYSTEM_012", 500, "数据删除失败"),
    DATA_INSERT_FAILED("SYSTEM_013", 500, "数据插入失败"),
    DATA_QUERY_FAILED("SYSTEM_014", 500, "数据查询失败"),

    PERMISSION_UNAUTHORIZED("SYSTEM_021", 401, "未登录"),
    PERMISSION_DENIED("SYSTEM_022", 403, "无权限"),

    OPERATION_REPEAT("SYSTEM_051", 409, "操作重复"),

    SYSTEM_ERROR("SYSTEM_100", 500, "系统错误"),

    // 业务错误 - 用户模块
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
    VERIFY_CODE_INVALID("USER_030", 400, "验证码错误"),


    USER_NOT_EXISTS("USER_051", 404, "用户不存在"),
    PASSWORD_ERROR("USER_052", 401, "密码错误"),


    CREDIT_TOO_LOW("USER_081", 400, "用户积分不足"),

    // 用户第三方绑定相关
    THIRD_AUTH_FAILED("USER_071", 401, "第三方授权失败"),
    THIRD_SYSTEM_ERROR("USER_072", 500, "第三方系统错误"),
    USER_THIRD_PARTY_UNBIND_FAILED("USER_073", 500, "第三方账号解绑失败"),
    USER_THIRD_PARTY_NOT_EXISTS("USER_074", 404, "第三方账号绑定记录不存在"),
    USER_THIRD_PARTY_NOT_BOUND("USER_075", 400, "第三方账号未绑定"),
    USER_THIRD_PARTY_BIND_FAILED("USER_076", 500, "第三方账号绑定失败"),

    // 帖子模块
    POST_NOT_EXISTS("POST_001", 404, "帖子不存在"),
    POST_TITLE_NULL("POST_002", 400, "帖子标题为空"),
    POST_CONTENT_NULL("POST_003", 400, "帖子内容为空"),
    POST_ID_NULL("POST_004", 400, "帖子ID为空"),
    POST_STATUS_ERROR("POST_005", 400, "帖子状态参数错误"),

    POST_STATUS_INVALID("POST_021", 400, "帖子状态参数错误"),
    POST_TITLE_INVALID("POST_022", 400, "帖子标题不符合规范"),
    POST_CONTENT_INVALID("POST_023", 400, "帖子内容不符合规范"),
    POST_IMAGE_FORMAT_INVALID("POST_024", 400, "帖子图片格式错误"),

    POST_IMAGE_TOO_MANY("POST_031", 400, "帖子图片数量过多"),

    POST_ALREADY_DELETED("POST_051", 400, "帖子已删除"),

    TOP_POST_COUNT_EXCEED("POST_071", 400, "帖子置顶数量超出限制"),
    DAILY_LIKE_TIMES_EXCEED("POST_072", 400, "用户每天点赞次数超出限制"),
    POST_STATUS_ABNORMAL("POST_073", 400, "帖子状态异常"),

    USER_NOT_LIKED_POST("POST_081", 400, "用户未点赞该帖子"),
    POST_LIKE_COUNT_UPDATE_FAILED("POST_074", 500, "帖子点赞数量更新失败"),

    // 帖子跟帖模块
    POST_FOLLOW_NOT_EXISTS("POST_FOLLOW_001", 404, "跟帖不存在"),
    POST_FOLLOW_CONTENT_NULL("POST_FOLLOW_002", 400, "跟帖内容为空"),
    POST_FOLLOW_ID_NULL("POST_FOLLOW_003", 400, "跟帖ID为空"),
    POST_FOLLOW_STATUS_ERROR("POST_FOLLOW_004", 400, "跟帖状态参数错误"),

    POST_FOLLOW_CONTENT_ILLEGAL("POST_FOLLOW_011", 400, "跟帖内容不符合规范"),
    FOLLOW_STATUS_ILLEGAL("POST_FOLLOW_012", 400, "跟帖状态参数错误"),

    // 商品模块
    PRODUCT_NOT_EXISTS("PRODUCT_001", 404, "商品不存在"),
    PRODUCT_TITLE_NULL("PRODUCT_002", 400, "商品标题为空"),
    PRODUCT_PRICE_NULL("PRODUCT_003", 400, "商品价格为空"),
    PRODUCT_ID_NULL("PRODUCT_004", 400, "商品ID为空"),
    PRODUCT_CATEGORY_NULL("PRODUCT_005", 400, "商品分类为空"),

    PRODUCT_TITLE_INVALID("PRODUCT_011", 400, "商品标题不符合规范"),
    PRODUCT_STATUS_INVALID("PRODUCT_012", 400, "商品状态参数错误"),
    PRODUCT_CONDITION_INVALID("PRODUCT_013", 400, "商品成色参数错误"),
    PRODUCT_PRICE_INVALID("PRODUCT_014", 400, "商品价格参数错误"),
    PRODUCT_STOCK_INVALID("PRODUCT_015", 400, "商品库存参数错误"),
    PRODUCT_IMAGE_URL_INVALID("PRODUCT_016", 400, "商品图片URL参数错误"),

    PRODUCT_DESCRIPTION_TOO_LONG("PRODUCT_021", 400, "商品描述过长"),
    PRODUCT_IMAGE_TOO_MANY("PRODUCT_022", 400, "商品图片数量过多"),

    PRODUCT_ALREADY_OFF_SALE("PRODUCT_091", 400, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT("PRODUCT_092", 400, "商品库存不足"),
    ORDER_AMOUNT_ABNORMAL("ORDER_021", 400, "订单金额异常"),



    // 订单模块
    ORDER_NOT_EXISTS("ORDER_001", 404, "订单不存在"),
    ORDER_ID_NULL("ORDER_002", 400, "订单ID为空"),
    ORDER_AMOUNT_NULL("ORDER_003", 400, "订单金额为空"),
    ORDER_STATUS_NULL("ORDER_004", 400, "订单状态为空"),
    ORDER_BUYER_NULL("ORDER_005", 400, "订单买家为空"),
    ORDER_SELLER_NULL("ORDER_006", 400, "订单卖家为空"),

    ORDER_STATUS_INVALID("ORDER_004", 400, "订单状态参数错误"),

    STOCK_INSUFFICIENT("ORDER_021", 400, "商品库存不足"),

    ORDER_NOT_COMPLETED("ORDER_005", 400, "订单未完成"),


    // 评价模块
    EVALUATION_NOT_EXISTS("EVAL_001", 404, "评价不存在"),
    EVALUATION_ID_NULL("EVAL_002", 400, "评价ID为空"),
    EVALUATION_CONTENT_NULL("EVAL_003", 400, "评价内容为空"),
    EVALUATION_SCORE_NULL("EVAL_004", 400, "评价分数为空"),
    EVALUATION_ORDER_ID_NULL("EVAL_005", 400, "评价订单ID为空"),

    EVALUATION_REPORT_REASON_NULL("EVAL_006", 400, "评价举报理由为空"),


    EVALUATION_SCORE_INVALID("EVAL_021", 400, "评价分数参数错误"),
    EVALUATION_CONTENT_INVALID("EVAL_022", 400, "评价内容参数错误"),
    EVALUATION_IMAGE_URL_INVALID("EVAL_023", 400, "评价图片URL参数错误"),
    EVALUATION_TAGS_INVALID("EVAL_024", 400, "评价标签参数错误"),
    EVALUATION_STATUS_INVALID("EVAL_025", 400, "评价状态参数错误"),

    EVALUATION_REPORT_REASON_TOO_LONG("EVAL_041", 400, "评价举报理由过长"),


    ORDER_ALREADY_EVALUATED("EVAL_091", 400, "订单已评价"),
    SELLER_NOT_EXISTS("MSG_002", 404, "卖家不存在"),


    // 消息模块
    MESSAGE_NOT_EXISTS("MSG_001", 404, "消息不存在"),
    MESSAGE_ID_NULL("MSG_002", 400, "消息ID为空"),
    MESSAGE_CONTENT_NULL("MSG_003", 400, "消息内容为空"),
    MESSAGE_RECEIVER_NULL("MSG_004", 400, "消息接收者为空"),
    MESSAGE_SENDER_NULL("MSG_005", 400, "消息发送者为空"),
    MESSAGE_TYPE_NULL("MSG_006", 400, "消息类型为空"),


    MESSAGE_TYPE_INVALID("MSG_005", 400, "消息类型参数错误"),
    MESSAGE_STATUS_INVALID("MSG_006", 400, "消息状态参数错误"),
    MESSAGE_CONTENT_INVALID("MSG_007", 400, "消息内容参数错误"),

    RECEIVER_NOT_EXISTS("MSG_011", 404, "接收者不存在"),
    SENDER_NOT_EXISTS("MSG_012", 404, "发送者不存在"),


    MESSAGE_STATUS_TRANSITION_INVALID("MSG_004", 400, "消息状态转换错误"),

    // QA模块
    QA_ID_NULL("QA_001", 400, "QA记录ID为空"),
    QA_QUESTION_TEXT_NULL("QA_002", 400, "QA问题内容为空"),
    QA_NOT_EXISTS("QA_003", 404, "QA记录不存在"),

    // Log模块
    LOG_NOT_EXISTS("LOG_001", 404, "日志记录不存在"),
    LOG_TARGET_ID_NULL("LOG_002", 400, "日志目标ID为空"),
    LOG_TARGET_TYPE_NULL("LOG_003", 400, "日志目标类型为空"),
    LOG_ACTION_TYPE_NULL("LOG_004", 400, "日志操作类型为空"),
    LOG_TARGET_TYPE_INVALID("LOG_005", 400, "日志目标类型参数错误"),
    LOG_ACTION_TYPE_INVALID("LOG_006", 400, "日志操作类型参数错误"),

    // 验证码模块
    VERIFY_CODE_SEND_FAILED("VERIFY_001", 500, "验证码发送失败"),
    VERIFY_CODE_VALIDATE_FAILED("VERIFY_002", 400, "验证码验证失败"),
    VERIFY_CODE_NOT_EXISTS("VERIFY_003", 404, "验证码不存在"),
    VERIFY_CODE_EXPIRED("VERIFY_004", 400, "验证码已过期"),
    VERIFY_CODE_NOT_MATCH("VERIFY_005", 400, "验证码不匹配");


    // HTTP状态码
    // 200：成功操作
    // 400：客户端参数错误 / 请求非法
    // 401：未认证 / 认证失败
    // 403：权限不足
    // 404：资源不存在
    // 409：资源冲突（如重复创建）
    // 500：服务器内部错误

    private final String code;          // 业务错误码
    private final int standardCode;     // 标准错误码（参考HTTP状态码）
    private final String message;       // 错误信息

    ErrorCode(String code, int standardCode, String message) {
        this.code = code;
        this.standardCode = standardCode;
        this.message = message;
    }
}