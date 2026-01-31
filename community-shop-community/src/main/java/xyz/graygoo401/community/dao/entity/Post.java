package xyz.graygoo401.community.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.community.enums.PostStatusEnum;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@NoArgsConstructor
@Data
@TableName("post")
public class Post {

    /** 帖子ID */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 点赞数 */
    @TableField("like_count")
    private Integer likeCount;

    /** 跟帖数 */
    @TableField("post_follow_count")
    private Integer postFollowCount;

    /** 标题 */
    @TableField("title")
    private String title;

    /** 内容 */
    @TableField("content")
    private String content;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否为热门帖 */
    @TableField("is_hot")
    private Boolean isHot;

    /** 是否为精华帖 */
    @TableField("is_essence")
    private Boolean isEssence;

    /** 是否为置顶帖 */
    @TableField("is_top")
    private Boolean isTop;

    /** 帖子状态 */
    @TableField("status")
    private PostStatusEnum status;

}
