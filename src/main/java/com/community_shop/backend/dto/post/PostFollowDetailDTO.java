package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.code.PostFollowStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟帖详情响应DTO（匹配PostFollowService.selectPostFollowById方法）
 */
@Data
@Schema(description = "跟帖详情响应数据")
public class PostFollowDetailDTO {

    /** 跟帖ID */
    @Schema(description = "跟帖ID", example = "3001")
    private Long postFollowId;

    /** 关联帖子ID */
    @Schema(description = "关联的帖子ID", example = "1001")
    private Long postId;

    /** 跟帖人信息 */
    @Schema(description = "跟帖人信息")
    private FollowerDTO follower;

    /** 跟帖内容 */
    @Schema(description = "跟帖内容", example = "这个商品真的很不错！")
    private String content;

    /** 跟帖点赞数 */
    @Schema(description = "跟帖点赞数", example = "5")
    private Integer likeCount;

    /** 当前用户是否已点赞 */
    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean isLiked;

    /** 跟帖状态（NORMAL-正常；HIDDEN-隐藏） */
    @Schema(description = "跟帖状态")
    private PostFollowStatusEnum status;

    /** 发布时间 */
    @Schema(description = "跟帖发布时间", example = "2023-10-01T15:30:00")
    private LocalDateTime createTime;

    /** 最后编辑时间（未编辑则为null） */
    @Schema(description = "最后编辑时间（未编辑则为null）", example = "2023-10-01T16:00:00")
    private LocalDateTime updateTime;

    /**
     * 跟帖人信息内部类
     */
    @Data
    @Schema(description = "跟帖人信息")
    public static class FollowerDTO {
        /** 跟帖人用户ID */
        @Schema(description = "跟帖人用户ID", example = "2002")
        private Long userId;

        /** 跟帖人用户名 */
        @Schema(description = "跟帖人用户名", example = "user456")
        private String username;

        /** 跟帖人头像URL */
        @Schema(description = "跟帖人头像URL", example = "https://example.com/avatar2.jpg")
        private String avatarUrl;
    }
}