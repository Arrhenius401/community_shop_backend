package com.community_shop.backend.dto.evaluation;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.SortEnum.EvaluationSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价列表查询DTO（匹配EvaluationService.getEvaluationList方法）
 * 用于接收评价列表的筛选条件和分页参数
 */
@Data
public class EvaluationQueryDTO extends PageParam {

    /** 商品ID（查询某商品的评价，非空） */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 评分筛选（可选：1-5，null表示全部） */
    private Integer score;

    /** 是否有图（可选：true-仅看有图评价；false-全部） */
    private Boolean hasImage;

    /** 排序字段（枚举：CREATE_TIME-评价时间；SCORE-评分；HELPFUL_COUNT-有用数） */
    private EvaluationSortFieldEnum sortField = EvaluationSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序） */
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
