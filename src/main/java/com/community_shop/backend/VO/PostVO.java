package com.community_shop.backend.VO;

import lombok.Data;

import java.util.List;

@Data
public class PostVO {
    // 帖子标题
    private String title;
    // 帖子内容（文本）
    private String content;
    // 图片列表（URL，支持多图，阿里云OSS存储）
    private List<String> imageUrls;
    // 所属主题吧名称
    private String barName;
}
