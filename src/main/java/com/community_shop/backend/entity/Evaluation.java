package com.community_shop.backend.entity;

import java.time.LocalDateTime;

public class Evaluation {
    private Long evalId; // 评价ID
    private Long orderId; // 关联订单ID
    private Long userId; // 评价者ID
    private String content; // 评价内容
    private Integer score; // 评分（1-5星）
    private LocalDateTime createTime; // 评价时间
}
