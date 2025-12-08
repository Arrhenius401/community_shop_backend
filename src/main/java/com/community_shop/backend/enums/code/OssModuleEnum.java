package com.community_shop.backend.enums.code;

import lombok.Getter;

/**
 * Oss模块枚举类
 */
public enum OssModuleEnum {

    /** 默认 */
    DEFAULT("DEFAULT", "无标识文件",  "default", 100 * 1024 * 1024),

    /** 头像 */
    PICTURE_AVATAR("PICTURE_AVATAR", "头像图片", "picture/avatar", 1024 * 1024),

    /** 帖子 */
    PICTURE_POST("PICTURE_POST", "帖子图片",  "picture/post", 5 * 1024 * 1024),

    /** 商品图 */
    PICTURE_PRODUCT("PICTURE_PRODUCT", "商品图片", "picture/product", 5 * 1024 * 1024);

    /** 状态码 */
    @Getter
    private String code;

    /** 描述 */
    @Getter
    private String desc;

    /** 基本 URL 路径 */
    @Getter
    private String urlPath;

    /** 数据最大大小（单位：B） */
    @Getter
    private long maxSize;

    OssModuleEnum(String code, String desc, String urlPath, long maxSize) {
        this.code = code;
        this.desc = desc;
        this.urlPath = urlPath;
        this.maxSize = maxSize;
    }

}
