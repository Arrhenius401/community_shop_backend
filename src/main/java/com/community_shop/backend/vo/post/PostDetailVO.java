package com.community_shop.backend.vo.post;

import com.community_shop.backend.entity.Post;
import lombok.Data;

import java.util.List;

@Data
public class PostDetailVO extends Post {
    // 帖子标题
    private String title;
    // 帖子内容（文本）
    private String content;
    // 图片列表（URL，支持多图，阿里云OSS存储）
    private List<String> imageUrls;

    // ------------------------------ 发帖者展示信息（关联User实体的展示字段） ------------------------------
    private Long publisherId;     // 发帖者ID（即Post实体的userId）
    private String publisherName; // 发帖者用户名（来自user表user_name）
    private String publisherAvatar; // 发帖者头像（来自user表profile_picture）
    private Integer publisherCredit; // 发帖者信用分（来自user表credit_score，可选）
}
