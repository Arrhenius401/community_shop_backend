package com.community_shop.backend.entity;

import com.community_shop.backend.dto.post.PostFollowPublishDTO;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
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
    private PostFollowStatusEnum status;


    public PostFollow(){}

    public PostFollow(Long postFollowId, Long postId, Long userId, String content, int likeCount, LocalDateTime createTime, LocalDateTime updateTime, PostFollowStatusEnum status) {
        this.postFollowId = postFollowId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.likeCount = likeCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.status = status;
    }

    public PostFollow(PostFollowPublishDTO postFollowPublishDTO) {
        this.postId = postFollowPublishDTO.getPostId();
        this.userId = postFollowPublishDTO.getUserId();
        this.content = postFollowPublishDTO.getContent();

        this.likeCount = 0;
        this.status = PostFollowStatusEnum.NORMAL;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}
