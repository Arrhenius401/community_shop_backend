package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.codeEnum.PostStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long postId;
    private Long userId;
    private int likeCount;
    private int commentCount;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isHot;
    private Boolean isEssence;
    private Boolean isTop;
    private PostStatusEnum status;

    public Post(){}

    public Post(Long postId, Long userId, int likeCount, int commentCount, String title, String content, LocalDateTime createTime,
                LocalDateTime updateTime, Boolean isHot, Boolean isEssence, Boolean isTop, PostStatusEnum status) {
        this.postId = postId;
        this.userId = userId;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isHot = isHot;
        this.isEssence = isEssence;
        this.isTop = isTop;
        this.status = status;
    }
}
