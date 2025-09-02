package com.community_shop.backend.DTO.other;

/**
 * 卖家评分DTO，封装calculateSellerScore方法的返回数据
 */
public class SellerScoreDTO {
    // 平均评分（1-5星，保留1位小数）
    private Double averageScore;
    // 好评率（百分比，保留2位小数）
    private Double positiveRate;
    // 好评数（4-5星）
    private Integer positiveCount;
    // 中评数（3星）
    private Integer middleCount;
    // 差评数（1-2星）
    private Integer negativeCount;
}
