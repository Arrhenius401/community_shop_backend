package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.PostStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long postID;
    private Long userID;
    private int likeCount;
    private int commentCount;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private Boolean isHot;
    private Boolean isEssence;
    private Boolean isTop;
    private PostStatusEnum status;
}
