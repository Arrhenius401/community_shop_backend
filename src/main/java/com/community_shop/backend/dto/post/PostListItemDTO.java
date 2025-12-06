package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.code.PostStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表项DTO（配合泛型PageResult使用）
 */
@Data
@Schema(description = "帖子列表项响应数据")
public class PostListItemDTO {

    /** 帖子ID（用于跳转详情） */
    @Schema(description = "帖子ID", example = "1001")
    private Long postId;

    /** 标题 */
    @Schema(description = "帖子标题", example = "社区超市新品推荐")
    private String title;

    /** 发布者信息 */
    @Schema(description = "发布者简易信息")
    private PublisherSimpleDTO publisher;

    /** 首图URL（无图则为默认图） */
    @Schema(description = "帖子首图URL", example = "https://example.com/cover.jpg")
    private String coverImage;

    /** 摘要 */
    @Schema(description = "帖子内容摘要", example = "今天超市到了一批新鲜水果...")
    private String summary;

    /** 点赞数 */
    @Schema(description = "帖子点赞数", example = "50")
    private Integer likeCount;

    /** 评论数 */
    @Schema(description = "帖子评论数", example = "10")
    private Integer commentCount;

    /** 是否为热门帖 */
    @Schema(description = "是否为热门帖", example = "true")
    private Boolean isHot;

    /** 是否为精华帖 */
    @Schema(description = "是否为精华帖", example = "true")
    private Boolean isEssence;

    /** 发布时间 */
    @Schema(description = "发布时间", example = "2023-10-01T14:30:00")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间", example = "2023-10-01T14:30:00")
    private LocalDateTime updateTime;

    /** 帖子状态 */
    @Schema(description = "帖子状态")
    private PostStatusEnum status;

    /**
     * 发布者极简信息内部类
     */
    @Data
    @Schema(description = "发布者极简信息")
    public static class PublisherSimpleDTO {
        /** 用户ID */
        @Schema(description = "发布者用户ID", example = "2001")
        private Long userId;

        /** 用户名 */
        @Schema(description = "发布者用户名", example = "user123")
        private String username;

        /** 头像URL */
        @Schema(description = "发布者头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;
    }
}