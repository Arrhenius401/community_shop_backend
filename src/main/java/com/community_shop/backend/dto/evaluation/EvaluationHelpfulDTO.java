package com.community_shop.backend.dto.evaluation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价有用操作DTO（匹配EvaluationService.updateHelpfulCount方法）
 * 用于处理用户“觉得评价有用”的点赞操作
 */
@Data
public class EvaluationHelpfulDTO {

    /** 评价ID（非空） */
    @NotNull(message = "评价ID不能为空")
    private Long evaluationId;

    /** 操作类型（true-标记有用；false-取消标记） */
    @NotNull(message = "操作类型不能为空")
    private Boolean isHelpful;
}
