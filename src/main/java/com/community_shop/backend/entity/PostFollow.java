package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.PostFollowStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostFollow {
    private Long postFollowID;
    private Long postID;
    private Long userID;
    private String content;
    private int likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean isDeleted;
    private PostFollowStatusEnum status;

    public PostFollow(){}

    public PostFollow(Long postFollowID, Long postID, Long userID, String content, int likeCount, LocalDateTime createTime, LocalDateTime updateTime, boolean isDeleted, PostFollowStatusEnum status) {
        this.postFollowID = postFollowID;
        this.postID = postID;
        this.userID = userID;
        this.content = content;
        this.likeCount = likeCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isDeleted = isDeleted;
        this.status = status;
    }
}
