package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户帖子点赞关系实体类
 */
@Data
@TableName("user_post_like")
public class UserPostLike {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID（关联user表的user_id） */
    private Long userId;

    /** 帖子ID（关联post表的post_id） */
    private Long postId;

    /** 点赞时间 */
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
