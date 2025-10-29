package com.community_shop.backend.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价有用操作DTO（匹配EvaluationService.updateHelpfulCount方法）
 * 用于处理用户“觉得评价有用”的点赞操作
 */
@Data
@Schema(description = "评价有用操作DTO，用于处理用户对评价的“有用”点赞/取消操作")
public class EvaluationHelpfulDTO {

    /** 评价ID（非空） */
    @NotNull(message = "评价ID不能为空")
    @Schema(description = "目标评价ID", example = "987654", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long evaluationId;

    /** 操作类型（true-标记有用；false-取消标记） */
    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型：true-标记有用，false-取消标记", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isHelpful;
}