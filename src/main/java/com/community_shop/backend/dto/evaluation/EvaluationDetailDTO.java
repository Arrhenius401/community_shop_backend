package com.community_shop.backend.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价详情响应DTO（匹配EvaluationService.getEvaluationById方法）
 * 用于返回单条评价的完整信息，包含评价人、商品、图片等关联数据
 */
@Data
public class EvaluationDetailDTO {

    /** 评价ID */
    private Long evaluationId;

    /** 订单ID（关联的订单） */
    private Long orderId;

    /** 商品信息 */
    private ProductSimpleDTO product;

    /** 评价人信息（脱敏展示） */
    private EvaluatorDTO evaluator;

    /** 评分（1-5星） */
    private Integer score;

    /** 评价内容 */
    private String content;

//    /** 评价图片URL列表 */
//    private List<String> imageUrls;
//
//    /** 评价标签 */
//    private List<String> tags;
//
//    /** 商家回复（可选，null表示未回复） */
//    private MerchantReplyDTO merchantReply;
//
//    /** 有用数（其他用户觉得有帮助的次数） */
//    private Integer helpfulCount;
//
//    /** 当前用户是否觉得有用（true/false） */
//    private Boolean isHelpful;

    /** 评价时间 */
    private LocalDateTime createTime;

//    /** 追评信息（可选，null表示未追评） */
//    private AdditionalEvaluationDTO additionalEvaluation;

    /** 商品简易信息内部类 */
    @Data
    @AllArgsConstructor
    public static class ProductSimpleDTO {
        private Long productId;
        private String productName;
        private String productImage; // 商品首图
    }

    /** 评价人信息内部类（脱敏） */
    @Data
    @AllArgsConstructor
    public static class EvaluatorDTO {
        private Long userId;
        private String username; // 用户名脱敏（如"张***"）
        private String avatarUrl;
    }

//    /** 商家回复内部类 */
//    @Data
//    public static class MerchantReplyDTO {
//        private String content;
//        private LocalDateTime replyTime;
//    }

//    /** 追评信息内部类 */
//    @Data
//    public static class AdditionalEvaluationDTO {
//        private String content;
//        private List<String> imageUrls;
//        private LocalDateTime createTime;
//    }
}
