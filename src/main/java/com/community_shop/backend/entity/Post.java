package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.enums.code.PostStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@Data
@TableName("post")
public class Post {

    /** 帖子ID */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    /** 用户ID */
    private Long userId;

    /** 点赞数 */
    private Integer likeCount;

    /** 跟帖数 */
    private Integer postFollowCount;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否为热门帖 */
    private Boolean isHot;

    /** 是否为精华帖 */
    private Boolean isEssence;

    /** 是否为置顶帖 */
    private Boolean isTop;

    /** 帖子状态 */
    private PostStatusEnum status;

    public Post(){}

    public Post(Long postId, Long userId, int likeCount, int postFollowCount, String title, String content, LocalDateTime createTime,
                LocalDateTime updateTime, Boolean isHot, Boolean isEssence, Boolean isTop, PostStatusEnum status) {
        this.postId = postId;
        this.userId = userId;
        this.likeCount = likeCount;
        this.postFollowCount = postFollowCount;
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
