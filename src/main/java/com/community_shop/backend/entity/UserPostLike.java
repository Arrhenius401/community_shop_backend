package com.community_shop.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPostLike {
    // 自增主键ID
    private Long id;

    // 用户ID（关联user表的user_id）
    private Long userId;

    // 帖子ID（关联post表的post_id）
    private Long postId;

    // 点赞时间（记录用户点赞的时间）
    private LocalDateTime likeTime;

    public UserPostLike(){}

    public UserPostLike(Long userId, Long postId, LocalDateTime likeTime) {
        this.userId = userId;
        this.postId = postId;
        this.likeTime = likeTime;
    }

    public UserPostLike(Long id, Long userId, Long postId, LocalDateTime likeTime) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.likeTime = likeTime;
    }
}
