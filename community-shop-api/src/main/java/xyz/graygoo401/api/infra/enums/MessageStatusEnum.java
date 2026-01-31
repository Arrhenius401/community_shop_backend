package xyz.graygoo401.api.infra.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态枚举类
 */
@AllArgsConstructor
@Getter
public enum MessageStatusEnum {

    /** 未读状态：用户尚未查看该消息 */
    UNREAD("UNREAD", "未读"),

    /** 已读状态：用户已查看该消息 */
    READ("READ", "已读"),

    /** 删除状态：用户删除了该消息（逻辑删除标记） */
    DELETED("DELETED", "已删除");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据code反向获取枚举对象
     */
    public static MessageStatusEnum getByCode(String code) {
        for (MessageStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
