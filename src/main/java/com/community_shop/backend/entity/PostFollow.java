package com.community_shop.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostFollow {
    Long postFollowID;
    Long postID;
    Long userID;
    String content;
    int likeCount;
    LocalDateTime createTime;
    LocalDateTime updateTime;
    boolean isDeleted;
}
