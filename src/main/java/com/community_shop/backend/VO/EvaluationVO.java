package com.community_shop.backend.VO;

import lombok.Data;

@Data
public class EvaluationVO {
    // 订单ID（非空，必须是已完成且未评价的订单）
    private Long orderId;

    // 评分（非空，1-5星）
    private Integer score;

    // 评价内容（非空，长度1-1000）
    private String content;

    // 评价图片（可选，最多3张，阿里云OSS存储）
    private String[] imageUrls;

    // 是否匿名评价（可选，默认false）
    private Boolean isAnonymous = false;
}
