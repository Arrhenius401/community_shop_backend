package xyz.graygoo401.api.trade.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 卖家评分DTO，封装calculateSellerScore方法的返回数据
 */
@Data
@Schema(description = "卖家评分统计DTO，包含卖家的评分分布及统计数据")
public class SellerScoreDTO {

    /** 卖家ID */
    @Schema(description = "卖家ID", example = "123456")
    private Long sellerId;

    /** 平均评分 */
    @Schema(description = "卖家平均评分（1-5星）", example = "4.8", type = "number", format = "double")
    private Double averageScore;

    /** 评价总数 */
    @Schema(description = "评价总数量", example = "1200")
    private Integer totalCount;

    /** 好评率 */
    @Schema(description = "好评率（百分比）", example = "95.5", type = "number", format = "double")
    private Double positiveRate;

    /** 差评率 */
    @Schema(description = "差评率（百分比）", example = "2.1", type = "number", format = "double")
    private Double negativeRate;

    /** 五星评价数 */
    @Schema(description = "五星评价数量", example = "980")
    private Integer FiveStarCount;

    /** 四星评价数 */
    @Schema(description = "四星评价数量", example = "150")
    private Integer FourStarCount;

    /** 三星评价数 */
    @Schema(description = "三星评价数量", example = "40")
    private Integer ThreeStarCount;

    /** 二星评价数 */
    @Schema(description = "二星评价数量", example = "20")
    private Integer TwoStarCount;

    /** 一星评价数 */
    @Schema(description = "一星评价数量", example = "10")
    private Integer OneStarCount;
}