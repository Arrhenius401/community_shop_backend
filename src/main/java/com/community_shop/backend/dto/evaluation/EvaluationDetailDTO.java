package com.community_shop.backend.dto.evaluation;

import com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价详情响应DTO（匹配EvaluationService.getEvaluationById方法）
 * 用于返回单条评价的完整信息，包含评价人、商品、图片等关联数据
 */
@Data
@Schema(description = "评价详情响应DTO，包含单条评价的完整信息及关联数据")
public class EvaluationDetailDTO {

    /** 评价ID */
    @Schema(description = "评价唯一标识ID", example = "987654")
    private Long evalId;

    /** 订单ID（关联的订单） */
    @Schema(description = "关联的订单ID", example = "654321")
    private Long orderId;

    /** 商品信息 */
    @Schema(description = "评价关联的商品简易信息")
    private ProductSimpleDTO product;

    /** 评价人信息（脱敏展示） */
    @Schema(description = "评价人信息（已脱敏）")
    private EvaluatorDTO evaluator;

    /** 评分（1-5星） */
    @Schema(description = "评价分数（1-5星）", example = "5")
    private Integer score;

    /** 评价内容 */
    @Schema(description = "评价详细内容", example = "商品质量很好，物流也很快，非常满意！")
    private String content;

    /** 评价时间 */
    @Schema(description = "评价创建时间", example = "2023-10-01T14:30:00")
    private LocalDateTime createTime;

    /** 评价状态 */
    @Schema(description = "评价状态（枚举）", example = "NORMAL")
    private EvaluationStatusEnum status;

    /** 商品简易信息内部类 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "商品简易信息，包含ID、名称和首图")
    public static class ProductSimpleDTO {
        /** 商品ID */
        @Schema(description = "商品ID", example = "123")
        private Long productId;

        /** 商品名称 */
        @Schema(description = "商品名称", example = "华为Mate 60 Pro")
        private String productName;

        /** 商品首图URL */
        @Schema(description = "商品首图URL", example = "https://example.com/images/product123.jpg")
        private String productImage; // 商品首图
    }

    /** 评价人信息内部类（脱敏） */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "评价人信息（脱敏处理）")
    public static class EvaluatorDTO {

        /** 评价人用户ID */
        @Schema(description = "评价人用户ID", example = "456")
        private Long userId;

        /** 脱敏用户名 */
        @Schema(description = "脱敏用户名（如“张***”）", example = "李***")
        private String username;

        /** 脱敏手机号 */
        @Schema(description = "用户头像URL", example = "https://example.com/avatars/user456.jpg")
        private String avatarUrl;
    }
}
