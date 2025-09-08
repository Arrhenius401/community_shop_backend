package com.community_shop.backend.dto.post;

import com.community_shop.backend.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDetailDTO extends Post {

    /** 发布者信息（脱敏） */
    private PublisherDTO publisher;

    /** 帖子ID */
    private Long postId;

    /** 标题 */
    private String title;

    /** 内容（已过滤敏感词） */
    private String content;

    /** 图片URL列表（数组格式，适配前端展示） */
    private String[] imageUrls;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数（跟帖数） */
    private Integer commentCount;

    /** 是否热门帖（0-否；1-是） */
    private Integer isHot;

    /** 当前用户是否已点赞（true/false，用于前端状态） */
    private Boolean isLiked;

    /** 发布时间 */
    private LocalDateTime createTime;

    /**
     * 发布者简易信息内部类
     */
    @Data
    public static class PublisherDTO {
        private Long userId;         // 发布者ID
        private String username;     // 用户名
        private String avatarUrl;    // 头像URL
        private Integer creditScore; // 信用分（展示可信度）
    }
}
