package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.dto.post.PostFollowPublishDTO;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子跟帖实体类
 */
@Data
@TableName("post_follow")
public class PostFollow {

    /** 跟帖ID */
    @TableId(value = "post_follow_id", type = IdType.AUTO)
    private Long postFollowId;

    /** 帖子ID */
    private Long postId;

    /** 用户ID */
    private Long userId;

    /** 跟帖父ID */
    private Long parentId;

    /** 跟帖内容 */
    private String content;

    /** 点赞数 */
    private int likeCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 状态 */
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

}
