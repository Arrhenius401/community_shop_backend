package xyz.graygoo401.api.community.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.graygoo401.api.community.enums.PostStatusEnum;

/**
 * 帖子状态更新请求DTO（匹配PostService.updatePostStatus方法）
 */
@Data
@Schema(description = "帖子状态更新请求参数")
public class PostStatusUpdateDTO {

    /** 帖子ID（非空，用于定位帖子） */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /** 目标状态（非空，需为PostStatusEnum中的合法值） */
    @NotNull(message = "目标状态不能为空")
    @Schema(description = "帖子目标状态", example = "PUBLISHED", requiredMode = Schema.RequiredMode.REQUIRED)
    private PostStatusEnum status;

    /** 操作人ID（非空，用于权限校验） */
    @Schema(description = "操作人用户ID（用于权限校验）", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorId;

    /** 状态变更原因（可选，如驳回原因、删除原因，最多200字） */
    @Schema(description = "状态变更原因（可选）", example = "内容包含敏感信息", maxLength = 200)
    private String reason;
}