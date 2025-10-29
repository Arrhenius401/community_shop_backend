package com.community_shop.backend.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子更新VO：封装用户更新帖子时可修改的字段
 * 仅包含用户有权限修改的内容，不包含状态、点赞数等系统维护字段
 */
@Data
@Schema(description = "帖子更新请求参数")
public class PostUpdateDTO {

    /** 帖子ID，非空 */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "要更新的帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId; // 必须携带，用于定位要更新的帖子

    /** 操作人ID（非空，用于权限校验） */
    @NotNull(message = "操作人ID不能为空")
    @Schema(description = "操作人用户ID（用于权限校验）", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorId;

    /** 帖子标题，非空，长度1-100字 */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "更新后的帖子标题", example = "社区超市新品推荐（更新版）", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title; // 允许修改标题

    /** 帖子内容，非空，长度1-5000字 */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    @Schema(description = "更新后的帖子内容（支持富文本）", example = "<p>今天超市到了一批新鲜水果，价格更优惠...</p>", maxLength = 5000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content; // 允许修改内容
}