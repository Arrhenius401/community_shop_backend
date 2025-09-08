package com.community_shop.backend.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子点赞请求DTO（匹配PostService.likePost方法）
 */
@Data
public class PostLikeDTO {

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 操作用户ID（当前登录用户） */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 操作类型（true-点赞；false-取消点赞） */
    @NotNull(message = "操作类型不能为空")
    private Boolean isLike;
}
