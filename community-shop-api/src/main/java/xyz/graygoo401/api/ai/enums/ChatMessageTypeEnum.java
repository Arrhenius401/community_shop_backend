package xyz.graygoo401.api.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.ai.chat.messages.MessageType;

/**
 * 自定义枚举适配器（适配 MyBatis-Plus 自动转换）
 */
@Getter
public enum ChatMessageTypeEnum {

    /** 用户消息 */
    USER(MessageType.USER),

    /** 助手消息 */
    ASSISTANT(MessageType.ASSISTANT),

    /** 系统消息 */
    SYSTEM(MessageType.SYSTEM),

    /** 工具消息 */
    TOOL(MessageType.TOOL);

    @JsonValue
    @EnumValue
    private final String code;

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

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static ChatMessageTypeEnum getByCode(String code) {
        for (ChatMessageTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}