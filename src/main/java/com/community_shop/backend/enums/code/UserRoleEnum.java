package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum UserRoleEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    // 普通用户：系统默认角色，拥有基础功能权限（发帖、购买商品等）
    USER("USER", "普通用户"),
    // 管理员：拥有系统管理权限（用户管理、内容审核等）
    ADMIN("ADMIN", "管理员");

    // 角色标识（存储到数据库）
    @EnumValue
    private final String code;
    // 角色描述（用于前端展示和日志记录）
    @Getter
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法
    // 在getCode()方法上添加@JsonValue注解，明确指定序列化时只输出 code 的值
    @JsonValue
    public String getCode() {
        return code;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static UserRoleEnum getByCode(String code) {
        for (UserRoleEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户角色code：" + code);
    }

}
