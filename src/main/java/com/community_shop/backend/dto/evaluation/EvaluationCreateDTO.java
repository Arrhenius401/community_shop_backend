package com.community_shop.backend.dto.evaluation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

/**
 * 评价创建请求VO（视图对象）
 * 用于接收前端传递的创建参数，适配评价内容更新的业务场景
 */
@Data
public class EvaluationCreateDTO implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 评价关联的订单ID（非空）
     * 必传参数，用于定位需要更新的评价记录
     * 关联evaluation表的order_id字段
     */
    @NotNull(message = "评价ID不能为空")
    private Long orderId;

    /**
     * 评价关联的用户ID（非空）
     * 必传参数，用于定位评价记录
     * 关联evaluation表的user_id字段
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 新评价分数
     * 必传参数，用于更新评价的分数
     * 值范围在1-5之间，确保分数合规
     */
    @NotNull(message = "评价分数不能为空")
    private Integer score;

    /**
     * 新评价内容
     * 必传参数，用于更新评价的文本内容
     * 长度限制为1-500字符，确保内容合规且不超出数据库字段长度
     */
    @NotBlank(message = "评价内容不能为空")
    @Length(min = 1, max = 500, message = "评价内容长度必须在1-500字符之间")
    private String content;

//    /** 评价图片URL列表（最多5张，可选） */
//    private List<String> imageUrls;
//
//    /** 评价标签（如“质量好”“物流快”，最多3个，可选） */
//    @Size(max = 3, message = "最多选择3个标签")
//    private List<String> tags;

    public EvaluationCreateDTO() {}

    public EvaluationCreateDTO(Long orderId, Long userId, Integer score, String content) {
        this.orderId = orderId;
        this.userId = userId;
        this.score = score;
        this.content = content;
    }
}
