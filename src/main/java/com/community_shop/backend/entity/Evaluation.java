package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("evaluation")    // 表名
public class Evaluation {

    /** 评价ID */
    @TableId(value = "eval_id", type = IdType.AUTO) // 主键自增长
    private Long evalId;

    /** 关联订单ID */
    private Long orderId;

    /** 评价者ID */
    private Long userId;

    /** 卖家ID */
    private Long sellerId;

    /** 评价内容 */
    private String content;

    /** 评分 */
    private Integer score;

    /** 评价状态 */
    private EvaluationStatusEnum status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    public Evaluation(){}

    public Evaluation(EvaluationCreateDTO evaluationCreateDTO) {
        this.orderId = evaluationCreateDTO.getOrderId();
        this.userId = evaluationCreateDTO.getUserId();
        this.content = evaluationCreateDTO.getContent();
        this.score = evaluationCreateDTO.getScore();

        this.createTime = LocalDateTime.now();

    }
}
