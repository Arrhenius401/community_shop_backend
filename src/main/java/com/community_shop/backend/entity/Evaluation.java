package com.community_shop.backend.entity;

import com.community_shop.backend.dto.evaluation.EvaluationCreateDTO;
import com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@AllArgsConstructor
@Data
public class Evaluation {
    private Long evalId; // 评价ID
    private Long orderId; // 关联订单ID
    private Long userId; // 评价者ID
    private Long sellerId;  // 卖家ID
    private String content; // 评价内容
    private Integer score; // 评分（1-5星）
    private EvaluationStatusEnum status;    // 评价状态
    private LocalDateTime createTime; // 评价时间
    private LocalDateTime updateTime;   // 更新时间

    public Evaluation(){}

    public Evaluation(EvaluationCreateDTO evaluationCreateDTO) {
        this.orderId = evaluationCreateDTO.getOrderId();
        this.userId = evaluationCreateDTO.getUserId();
        this.content = evaluationCreateDTO.getContent();
        this.score = evaluationCreateDTO.getScore();

        this.createTime = LocalDateTime.now();

    }
}
