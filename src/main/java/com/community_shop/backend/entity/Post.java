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

    public Post(){}

    public Post(Long postID, Long userID, int likeCount, int commentCount, String title, String content, LocalDateTime createTime, Boolean isHot, Boolean isEssence, Boolean isTop, PostStatusEnum status) {
        this.postID = postID;
        this.userID = userID;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.isHot = isHot;
        this.isEssence = isEssence;
        this.isTop = isTop;
        this.status = status;
    }
}
