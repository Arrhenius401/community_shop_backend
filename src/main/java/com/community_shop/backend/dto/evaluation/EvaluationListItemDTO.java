package com.community_shop.backend.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价列表项DTO（配合泛型PageResult使用）
 * 用于评价列表页展示，仅包含核心信息，减少数据传输
 */
@NoArgsConstructor
@Data
@Schema(description = "评价列表项DTO，用于评价列表页展示的核心信息")
public class EvaluationListItemDTO {

    /** 评价ID（用于跳转详情） */
    @Schema(description = "评价ID，用于跳转详情页", example = "987654")
    private Long evalId;

    /** 评价人信息 */
    @Schema(description = "评价人简易信息（脱敏）")
    private EvaluatorSimpleDTO evaluator;

    /** 评分（1-5星） */
    @Schema(description = "评价分数（1-5星）", example = "5")
    private Integer score;

    /** 评价内容摘要（前100字） */
    @Schema(description = "评价内容摘要（前100字）", example = "商品质量很好，物流也很快...")
    private String contentSummary;

    /** 评价图片缩略图（最多3张，无图则为空） */
    @Schema(description = "评价图片缩略图URL列表（最多3张）", example = "[\"https://example.com/thumb1.jpg\"]")
    private List<String> imageThumbs;

    /** 评价标签（最多显示2个） */
    @Schema(description = "评价标签列表（最多2个）", example = "[\"质量好\", \"物流快\"]")
    private List<String> tags;

    /** 有用数 */
    @Schema(description = "评价的有用数（点赞数）", example = "28")
    private Integer helpfulCount;

    /** 评价时间 */
    @Schema(description = "评价创建时间", example = "2023-10-01T14:30:00")
    private LocalDateTime createTime;

    /** 是否有追评（true/false） */
    @Schema(description = "是否有追评", example = "false")
    private Boolean hasAdditional;

    /** 评价人极简信息内部类 */
    @Data
    @Schema(description = "评价人极简信息（脱敏）")
    public static class EvaluatorSimpleDTO {
        /** 脱敏用户名 */
        @Schema(description = "脱敏用户名（如“张***”）", example = "王***")
        private String username;

        /** 用户头像URL */
        @Schema(description = "用户头像URL", example = "https://example.com/avatars/user789.jpg")
        private String avatarUrl;
    }
}