package com.community_shop.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Evaluation {
    private Long evalID; // 评价ID
    private Long orderID; // 关联订单ID
    private Long userID; // 评价者ID
    private String content; // 评价内容
    private Integer score; // 评分（1-5星）
    private LocalDateTime createTime; // 评价时间
}
