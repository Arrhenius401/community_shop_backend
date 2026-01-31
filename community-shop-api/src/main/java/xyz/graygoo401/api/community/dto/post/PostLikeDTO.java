package xyz.graygoo401.api.community.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子点赞请求DTO（匹配PostService.likePost方法）
 */
@Data
@Schema(description = "帖子点赞/取消点赞请求参数")
public class PostLikeDTO {

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /** 操作用户ID（当前登录用户） */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "操作用户ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /** 操作类型（true-点赞；false-取消点赞） */
    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型（true-点赞，false-取消点赞）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isLike;
}