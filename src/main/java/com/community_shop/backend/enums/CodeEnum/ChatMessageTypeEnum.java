package com.community_shop.backend.enums.CodeEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import org.springframework.ai.chat.messages.MessageType;

/**
 * 自定义枚举适配器（适配 MyBatis-Plus 自动转换）
 */
public enum ChatMessageTypeEnum {
    USER(MessageType.USER),
    ASSISTANT(MessageType.ASSISTANT),
    SYSTEM(MessageType.SYSTEM),
    TOOL(MessageType.TOOL);

    // 对应数据库的字符串值（如 "user"）
    @EnumValue
    private final String code;
    // 关联 Spring AI 的 MessageType
    private final MessageType aiMessageType;

    ChatMessageTypeEnum(MessageType aiMessageType) {
        this.code = aiMessageType.getValue();
        this.aiMessageType = aiMessageType;
    }

    /**
     * 数据库字符串 → 自定义枚举
     */
    public static ChatMessageTypeEnum fromValue(String value) {
        for (ChatMessageTypeEnum type : values()) {
            if (type.code.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知消息类型: " + value);
    }

    /**
     * 自定义枚举 → Spring AI MessageType
     */
    public MessageType toAiMessageType() {
        return aiMessageType;
    }
}