package com.community_shop.backend.dto.evaluation;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.sort.EvaluationSortFieldEnum;
import com.community_shop.backend.enums.sort.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价列表查询DTO（匹配EvaluationService.getEvaluationList方法）
 * 用于接收评价列表的筛选条件和分页参数
 */
@Data
@Schema(description = "评价列表查询参数DTO，用于接收评价列表的筛选条件和分页参数")
public class EvaluationQueryDTO extends PageParam {

    /** 被评论者家ID（如查询某卖家的评价） */
    @NotNull(message = "被评论者ID不能为空")
    @Schema(description = "被评论者ID（如卖家ID）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long evaluateeId;

    /** 商品ID（查询某商品的评价） */
    @Schema(description = "商品ID，用于筛选特定商品的评价，null表示查询全部商品", example = "789012")
    private Long productId;

    /** 评分筛选（可选：1-5，null表示全部） */
    @Schema(description = "评分筛选，1-5星，null表示查询全部评分", example = "5", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer score;

    /** 是否有图（可选：true-仅看有图评价；false-全部） */
    @Schema(description = "是否筛选有图评价，true-仅看有图，false-全部", example = "true")
    private Boolean hasImage;

    /** 排序字段（枚举：CREATE_TIME-评价时间；SCORE-评分；HELPFUL_COUNT-有用数） */
    @Schema(description = "排序字段", example = "CREATE_TIME", defaultValue = "CREATE_TIME")
    private EvaluationSortFieldEnum sortField = EvaluationSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序） */
    @Schema(description = "排序方向", example = "DESC", defaultValue = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}