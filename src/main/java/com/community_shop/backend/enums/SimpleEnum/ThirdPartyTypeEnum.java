package com.community_shop.backend.enums.SimpleEnum;

/**
 * 第三方登录类型枚举（文档1第三方注册需求）
 */
public enum ThirdPartyTypeEnum {
    // 微信登录
    WECHAT("WECHAT"),
    // QQ登录
    QQ("QQ"),
    // github登录
    GITHUB("GITHUB");

    private final String type;

    ThirdPartyTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
