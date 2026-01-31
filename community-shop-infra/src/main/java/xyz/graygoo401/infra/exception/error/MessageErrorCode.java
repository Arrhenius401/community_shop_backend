package xyz.graygoo401.infra.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 消息模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum MessageErrorCode implements IErrorCode {

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

    MESSAGE_STATUS_TRANSITION_INVALID("MSG_004", 400, "消息状态转换错误");

    private final String code;
    private final int standardCode;
    private final String message;

}
