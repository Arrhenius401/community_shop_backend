package xyz.graygoo401.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举类
 */
@AllArgsConstructor
@Getter
public enum UserStatusEnum {

    /** 正常状态 */
    NORMAL("NORMAL", "正常状态"),

    /** 封禁状态 */
    BANNED("BANNED", "封禁状态"),

    /** 未激活状态 */
    INACTIVE("INACTIVE", "未激活"),

    /** 删除状态 */
    DELETED("DELETED", "删除");

    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static UserStatusEnum getByCode(String code) {
        for (UserStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
