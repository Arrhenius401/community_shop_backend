package com.community_shop.backend.entity;

import com.community_shop.backend.dto.evaluation.EvaluationCreateDTO;
import com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Evaluation {
    private Long evalId; // 评价ID
    private Long orderId; // 关联订单ID
    private Long userId; // 评价者ID
    private Long sellerId;  // 卖家ID
    private String content; // 评价内容
    private Integer score; // 评分（1-5星）
    private LocalDateTime createTime; // 评价时间
    private LocalDateTime updateTime;   // 更新时间
    private EvaluationStatusEnum status;    // 评价状态

    public Evaluation(){}

    public Evaluation(Long evalId, Long orderId, Long userId, Long sellerId, String content, Integer score,
                      LocalDateTime createTime, LocalDateTime updateTime, EvaluationStatusEnum status) {
        this.evalId = evalId;
        this.orderId = orderId;
        this.userId = userId;
        this.sellerId = sellerId;
        this.content = content;
        this.score = score;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.status = status;
    }

    public Evaluation(EvaluationCreateDTO evaluationCreateDTO) {
        this.orderId = evaluationCreateDTO.getOrderId();
        this.userId = evaluationCreateDTO.getUserId();
        this.content = evaluationCreateDTO.getContent();
        this.score = evaluationCreateDTO.getScore();

        this.createTime = LocalDateTime.now();

    }
}
