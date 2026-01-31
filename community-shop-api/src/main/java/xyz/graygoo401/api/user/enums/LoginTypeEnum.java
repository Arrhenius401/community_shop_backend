package xyz.graygoo401.api.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 */
@AllArgsConstructor
@Getter
public enum LoginTypeEnum {

    /** 手机号登录 */
    PHONE_NUMBER("PHONE_NUMBER", "手机号登录"),

    /** 邮箱登录 */
    EMAIL("EMAIL", "邮箱登录"),

    /** 第三方登录 */
    THIRD_PARTY("THIRD_PARTY", "第三方登录");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static LoginTypeEnum getByCode(String code) {
        for (LoginTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

}
