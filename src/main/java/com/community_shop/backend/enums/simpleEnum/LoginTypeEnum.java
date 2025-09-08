package com.community_shop.backend.enums.simpleEnum;

/**
 * 登录类型枚举
 */
public enum LoginTypeEnum {
    PASSWORD("password"),
    PHONE_NUMBER("phoneNumber"),
    EMAIL("email"),
    THIRD_PARTY("thirdParty");

    private String code;

    LoginTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
