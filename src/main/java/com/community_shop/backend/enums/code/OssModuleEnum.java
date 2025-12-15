package com.community_shop.backend.enums.code;

import lombok.Getter;

/**
 * Oss模块枚举类
 */
public enum OssModuleEnum {

    /** 默认 */
    DEFAULT("DEFAULT", "无标识文件",  "default", 10 * 1024 * 1024),

    // ==================== 图片 ====================
    /** 头像图片 */
    PICTURE_AVATAR("PICTURE_AVATAR", "头像图片", "picture/avatar", 1024 * 1024),

    /** 帖子图片 */
    PICTURE_POST("PICTURE_POST", "帖子图片",  "picture/post", 5 * 1024 * 1024),

    /** 商品图片 */
    PICTURE_PRODUCT("PICTURE_PRODUCT", "商品图片", "picture/product", 5 * 1024 * 1024),

    /** 系统图片 */
    PICTURE_SYSTEM("PICTURE_SYSTEM", "系统图片", "picture/system", 10 * 1024 * 1024),

    // ==================== 视频 ====================
    /** 帖子视频 */
    VIDEO_POST("VIDEO_POST", "帖子视频", "video/post", 100 * 1024 * 1024),

    /** 商品视频 */
    VIDEO_PRODUCT("VIDEO_PRODUCT", "商品视频", "video/product", 200 * 1024 * 1024),

    /** 系统视频 */
    VIDEO_SYSTEM("VIDEO_SYSTEM", "系统视频", "video/system", 300 * 1024 * 1024),

    // ==================== 音频 ====================
    /** 系统音频 */
    AUDIO_SYSTEM("AUDIO_SYSTEM", "系统音频", "audio/system", 30 * 1024 * 1024);

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
