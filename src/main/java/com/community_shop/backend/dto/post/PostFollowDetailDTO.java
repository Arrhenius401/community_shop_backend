package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟帖详情响应DTO（匹配PostFollowService.selectPostFollowById方法）
 */
@Data
public class PostFollowDetailDTO {

    /** 跟帖ID */
    private Long postFollowId;

    /** 关联帖子ID */
    private Long postId;

    /** 跟帖人信息 */
    private FollowerDTO follower;

    /** 跟帖内容 */
    private String content;

    /** 跟帖点赞数 */
    private Integer likeCount;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 跟帖状态（NORMAL-正常；HIDDEN-隐藏） */
    private PostFollowStatusEnum status;

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 最后编辑时间（未编辑则为null） */
    private LocalDateTime updateTime;

    /**
     * 跟帖人信息内部类
     */
    @Data
    public static class FollowerDTO {
        private Long userId;
        private String username;
        private String avatarUrl;
    }
}
