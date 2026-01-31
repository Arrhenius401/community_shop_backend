package xyz.graygoo401.api.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话状态枚举类
 */
@AllArgsConstructor
@Getter
public enum ChatSessionStatusEnum {

    /** 会话已开始 */
    STARTED("STARTED", "会话已开始"),

    /** 会话进行中 */
    ACTIVE("ACTIVE", "会话进行中"),

    /** 会话已关闭 */
    CLOSED("CLOSED", "会话已关闭"),

    /** 会话已超时 */
    TIMEOUT("TIMEOUT", "会话已超时"),

    /** 会话异常 */
    ERROR("ERROR", "会话异常");

    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static ChatSessionStatusEnum getByCode(String code) {
        for (ChatSessionStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
