package xyz.graygoo401.community.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import xyz.graygoo401.api.community.enums.PostFollowStatusEnum;

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
    @TableField(value = "post_id")
    private Long postId;

    /** 用户ID */
    @TableField(value = "user_id")
    private Long userId;

    /** 跟帖父ID */
    @TableField(value = "parent_id")
    private Long parentId;

    /** 跟帖内容 */
    @TableField(value = "content")
    private String content;

    /** 点赞数 */
    @TableField(value = "like_count")
    private int likeCount;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 状态 */
    @TableField(value = "status")
    private PostFollowStatusEnum status;
}
