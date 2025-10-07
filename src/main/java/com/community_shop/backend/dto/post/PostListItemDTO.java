package com.community_shop.backend.dto.post;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表项DTO（配合泛型PageResult使用）
 */
@Data
public class PostListItemDTO {

    /** 帖子ID（用于跳转详情） */
    private Long postId;

    /** 标题 */
    private String title;

    /** 发布者信息 */
    private PublisherSimpleDTO publisher;

    /** 首图URL（无图则为默认图） */
    private String coverImage;

    /** 摘要 */
    private String summary;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 发布时间 */
    private LocalDateTime createTime;

    /**
     * 发布者极简信息内部类
     */
    @Data
    public static class PublisherSimpleDTO {
        private Long userId;
        private String username;
    }
}
