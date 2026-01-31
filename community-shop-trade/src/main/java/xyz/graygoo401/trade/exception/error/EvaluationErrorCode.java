package xyz.graygoo401.trade.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 评价模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum EvaluationErrorCode implements IErrorCode {

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
    SELLER_NOT_EXISTS("MSG_002", 404, "卖家不存在");

    private final String code;
    private final int standardCode;
    private final String message;

}
