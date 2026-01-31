package xyz.graygoo401.api.infra.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举类
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum {

    /** 系统消息 */
    SYSTEM("SYSTEM", "系统消息"),

    /** 订单消息 */
    ORDER("ORDER", "订单消息"),

    /** 私信消息 */
    PRIVATE("PRIVATE", "私信消息");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据code反向获取枚举对象
     */
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的消息状态code：" + code);
    }
}
