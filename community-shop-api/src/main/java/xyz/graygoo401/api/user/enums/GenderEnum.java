package xyz.graygoo401.api.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举类
 */
@AllArgsConstructor
@Getter
public enum GenderEnum {
    /** 男 */
    MALE("MALE", "男"),

    /** 女 */
    FEMALE("FEMALE", "女"),

    /** 未知 */
    UNKNOWN("UNKNOWN", "未知");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static GenderEnum getByCode(String code) {
        for (GenderEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
