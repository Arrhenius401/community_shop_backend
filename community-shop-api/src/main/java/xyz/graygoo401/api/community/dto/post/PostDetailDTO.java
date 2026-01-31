package xyz.graygoo401.api.community.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.community.enums.PostStatusEnum;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子详情响应数据")
public class PostDetailDTO {

    /** 发布者信息（脱敏） */
    @Schema(description = "发布者信息")
    private PublisherDTO publisher;

    /** 帖子ID */
    @Schema(description = "帖子ID", example = "1001")
    private Long postId;

    /** 标题 */
    @Schema(description = "帖子标题", example = "社区超市新品推荐")
    private String title;

    /** 内容（已过滤敏感词） */
    @Schema(description = "帖子内容（已过滤敏感词）", example = "<p>今天超市到了一批新鲜水果...</p>")
    private String content;

    /** 图片URL列表（数组格式，适配前端展示） */
    @Schema(description = "帖子图片URL列表", example =  "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
    private String[] imageUrls;

    /** 点赞数 */
    @Schema(description = "帖子点赞数", example = "50")
    private Integer likeCount;

    /** 评论数（跟帖数） */
    @Schema(description = "帖子评论数（跟帖数）", example = "10")
    private Integer commentCount;

    /** 是否为热门帖 */
    @Schema(description = "是否为热门帖", example = "true")
    private Boolean isHot;

    /** 是否为精华帖 */
    @Schema(description = "是否为精华帖", example = "true")
    private Boolean isEssence;

    /** 是否为置顶帖 */
    @Schema(description = "是否为置顶帖", example = "false")
    private Boolean isTop;

    /** 当前用户是否已点赞（true/false，用于前端状态） */
    @Schema(description = "当前用户是否已点赞", example = "true")
    private Boolean isLiked;

    /** 发布时间 */
    @Schema(description = "发布时间", example = "2023-10-01T14:30:00")
    private LocalDateTime createTime;

    /** 帖子修改时间 */
    @Schema(description = "帖子修改时间", example = "2023-10-01T14:30:00")
    private LocalDateTime updateTime;

    /** 帖子状态 */
    @Schema(description = "帖子状态")
    private PostStatusEnum status;

    /**
     * 发布者简易信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "发布者信息")
    public static class PublisherDTO {
        /** 用户ID */
        @Schema(description = "发布者用户ID", example = "2001")
        private Long userId;

        /** 用户名 */
        @Schema(description = "发布者用户名", example = "user123")
        private String username;

        /** 头像URL */
        @Schema(description = "发布者头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;

        /** 信用分 */
        @Schema(description = "发布者信用分", example = "95")
        private Integer creditScore;
    }
}