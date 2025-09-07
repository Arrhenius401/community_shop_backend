package com.community_shop.backend.dto.evaluation;

import lombok.Data;

/**
 * 卖家评分DTO，封装calculateSellerScore方法的返回数据
 */
@Data
public class SellerScoreDTO {
    /**
     * 卖家ID
     */
    private Long sellerId;

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
