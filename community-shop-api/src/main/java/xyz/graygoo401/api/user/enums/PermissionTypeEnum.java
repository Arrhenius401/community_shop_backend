package xyz.graygoo401.api.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型枚举（商品 / 帖子发布）
 */
@AllArgsConstructor
@Getter
public enum PermissionTypeEnum {

    /** 商品发布权限 */
    PUBLISH_PRODUCT("PUBLISH_PRODUCT", "商品发布权限"),

    /** 帖子发布权限 */
    PUBLISH_POST("PUBLISH_POST", "帖子发布权限");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static PermissionTypeEnum getByCode(String code) {
        for (PermissionTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
