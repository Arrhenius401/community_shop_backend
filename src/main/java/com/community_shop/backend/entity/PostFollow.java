package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.PostFollowStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostFollow {
    private Long postFollowId;
    private Long postId;
    private Long userId;
    private String content;
    private int likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean isDeleted;
    private PostFollowStatusEnum status;

    public PostFollow(){}

    public PostFollow(Long postFollowId, Long postId, Long userId, String content, int likeCount, LocalDateTime createTime, LocalDateTime updateTime, boolean isDeleted, PostFollowStatusEnum status) {
        this.postFollowId = postFollowId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.likeCount = likeCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isDeleted = isDeleted;
        this.status = status;
    }
}
