package com.community_shop.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    Long postID;
    Long userID;
    String title;
    String content;
    int likeCount;
    int commentCount;
    LocalDateTime createTime;
    String status;
    Boolean ishot;
    String username;

}
