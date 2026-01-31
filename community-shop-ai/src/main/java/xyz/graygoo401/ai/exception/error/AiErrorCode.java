package xyz.graygoo401.ai.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * AI服务错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum AiErrorCode implements IErrorCode {

    // AI 模块
    AI_SERVICE_FAILS("AI_001", 500, "AI服务不可用"),
    AI_PARSE_FAILS("AI_002", 500, "AI服务前置解析失败"),
    AI_CLEAR_CONTENT_FAILS("AI_003", 500, "AI清理上下文"),

    AI_CHAT_SESSION_NOT_EXISTS("AI_101", 404, "会话不存在"),
    AI_CHAT_SESSION_FIRST_PROMPT_NULL("AI_CHAT_SESSION_102", 400, "会话首次消息为空"),

    AI_CHAT_MESSAGE_NOT_EXISTS("CHAT_MESSAGE_201", 404, "聊天消息不存在");

    private final String code;
    private final int standardCode;
    private final String message;

}
