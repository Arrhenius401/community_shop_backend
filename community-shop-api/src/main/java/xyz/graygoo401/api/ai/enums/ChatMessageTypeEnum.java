package xyz.graygoo401.api.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自定义枚举适配器（适配 MyBatis-Plus 自动转换）
 */
@AllArgsConstructor
@Getter
public enum ChatMessageTypeEnum {

    /** 用户消息 */
    USER("USER", "用户信息"),

    /** 助手消息 */
    ASSISTANT("ASSISTANT", "助手信息"),

    /** 系统消息 */
    SYSTEM("SYSTEM", "系统信息"),

    /** 工具消息 */
    TOOL("TOOL", "工具信息");

    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

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