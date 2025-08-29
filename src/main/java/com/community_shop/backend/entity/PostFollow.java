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
}
