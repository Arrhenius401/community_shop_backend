package com.community_shop.backend.enums.code;

import lombok.Getter;

/**
 * Oss模块枚举类
 */
public enum OssModuleEnum {

    /** 头像 */
    PICTURE_AVATAR("PICTURE_AVATAR", "picture/avatar", 1024 * 1024),

    /** 帖子 */
    PICTURE_POST("PICTURE_POST", "picture/post", 5 * 1024 * 1024),

    /** 商品图 */
    PICTURE_PRODUCT("PICTURE_PRODUCT", "picture/product", 5 * 1024 * 1024);

    /** 状态码 */
    @Getter
    private String code;

    /** 基本 URL 路径 */
    @Getter
    private String urlPath;

    /** 数据最大大小 */
    @Getter
    private long maxSize;

    OssModuleEnum(String code, String urlPath, long maxSize) {
        this.code = code;
        this.urlPath = urlPath;
        this.maxSize = maxSize;
    }

}
