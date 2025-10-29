package com.community_shop.backend.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品评分DTO
 */
@Data
@Schema(description = "商品评分统计DTO，包含商品的评分分布及统计数据")
public class ProductScoreDTO {

    /** 商品id */
    @Schema(description = "商品ID", example = "123456")
    private Long productId;

    /** 平均评分 */
    @Schema(description = "商品平均评分（1-5星）", example = "4.7", type = "number", format = "double")
    private Double averageScore;

    /** 评价总数 */
    @Schema(description = "评价总数量", example = "850")
    private Integer totalCount;

    /** 好评率 */
    @Schema(description = "好评率（百分比）", example = "94.2", type = "number", format = "double")
    private Double positiveRate;

    /** 差评率 */
    @Schema(description = "差评率（百分比）", example = "2.8", type = "number", format = "double")
    private Double negativeRate;

    /** 五星评价数 */
    @Schema(description = "五星评价数量", example = "680")
    private Integer FiveStarCount;

    /** 四星评价数 */
    @Schema(description = "四星评价数量", example = "120")
    private Integer FourStarCount;

    /** 三星评价数 */
    @Schema(description = "三星评价数量", example = "30")
    private Integer ThreeStarCount;

    /** 二星评价数 */
    @Schema(description = "二星评价数量", example = "15")
    private Integer TwoStarCount;

    /** 一星评价数 */
    @Schema(description = "一星评价数量", example = "5")
    private Integer OneStarCount;
}