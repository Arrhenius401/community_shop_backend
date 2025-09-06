package com.community_shop.backend.DTO.other;

import lombok.Data;

/**
 * 商品评分DTO
 */
@Data
public class ProductScoreDTO {

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 平均评分
     */
    private Double averageScore;
    /**
     * 评价总数
     */
    private Integer totalCount;

    /**
     * 好评率
     */
    private Double positiveRate;

    /**
     * 差评率
     */
    private Double negativeRate;

    /**
     * 五星评价数
     */
    private Integer FiveStarCount;

    /**
     * 四星评价数
     */
    private Integer FourStarCount;

    /**
     * 三星评价数
     */
    private Integer ThreeStarCount;

    /**
     * 二星评价数
     */
    private Integer TwoStarCount;

    /**
     * 一星评价数
     */
    private Integer OneStarCount;
}
