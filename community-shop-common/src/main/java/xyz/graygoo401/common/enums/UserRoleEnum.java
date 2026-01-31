package xyz.graygoo401.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举类
 */
@AllArgsConstructor
@Getter
public enum UserRoleEnum {

    /** 普通用户：系统默认角色，拥有基础功能权限（发帖、购买商品等） */
    USER("USER", "普通用户"),

    /** 管理员：拥有系统管理权限（用户管理、内容审核等） */
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static UserRoleEnum getByCode(String code) {
        for (UserRoleEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
