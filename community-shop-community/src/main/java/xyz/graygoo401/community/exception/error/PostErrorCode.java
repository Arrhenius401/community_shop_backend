package xyz.graygoo401.community.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 帖子模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum PostErrorCode implements IErrorCode {

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

    CREDIT_TOO_LOW("POST_081", 400, "用户积分不足"),

    // 帖子跟帖模块
    POST_FOLLOW_NOT_EXISTS("POST_FOLLOW_001", 404, "跟帖不存在"),
    POST_FOLLOW_CONTENT_NULL("POST_FOLLOW_002", 400, "跟帖内容为空"),
    POST_FOLLOW_ID_NULL("POST_FOLLOW_003", 400, "跟帖ID为空"),
    POST_FOLLOW_STATUS_ERROR("POST_FOLLOW_004", 400, "跟帖状态参数错误"),

    POST_FOLLOW_CONTENT_ILLEGAL("POST_FOLLOW_011", 400, "跟帖内容不符合规范"),
    FOLLOW_STATUS_ILLEGAL("POST_FOLLOW_012", 400, "跟帖状态参数错误"),

    // 帖子点赞模块
    USER_NOT_LIKED_POST("POST_LIKE_081", 400, "用户未点赞该帖子"),
    POST_LIKE_COUNT_UPDATE_FAILED("POST_LIKE_074", 500, "帖子点赞数量更新失败");

    private final String code;
    private final int standardCode;
    private final String message;

}
