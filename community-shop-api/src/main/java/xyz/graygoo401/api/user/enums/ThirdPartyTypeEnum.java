package xyz.graygoo401.api.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 第三方登录类型枚举（文档1第三方注册需求）
 */
@AllArgsConstructor
@Getter
public enum ThirdPartyTypeEnum {

    /** 微信登录 */
    WECHAT("WECHAT", "微信"),

    /** QQ登录 */
    QQ("QQ", "QQ"),

    /** Github登录 */
    GITHUB("GITHUB", "Github");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static ThirdPartyTypeEnum getByCode(String code) {
        for (ThirdPartyTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
