package xyz.graygoo401.trade.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.trade.enums.EvaluationStatusEnum;

import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("evaluation")    // 表名
public class Evaluation {

    /** 评价ID */
    @TableId(value = "eval_id", type = IdType.AUTO) // 主键自增长
    private Long evalId;

    /** 关联订单ID */
    @TableField("order_id")
    private Long orderId;

    /** 评价者ID */
    @TableField("user_id")
    private Long userId;

    /** 卖家ID */
    @TableField("seller_id")
    private Long evaluateeId;

    /** 评价内容 */
    @TableField("content")
    private String content;

    /** 评分 */
    @TableField("score")
    private Integer score;

    /** 评价状态 */
    @TableField("status")
    private EvaluationStatusEnum status;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
