package com.community_shop.backend.dto.evaluation;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价列表项DTO（配合泛型PageResult使用）
 * 用于评价列表页展示，仅包含核心信息，减少数据传输
 */
@Data
public class EvaluationListItemDTO {

    /** 评价ID（用于跳转详情） */
    private Long evalId;

    /** 评价人信息 */
    private EvaluatorSimpleDTO evaluator;

    /** 评分（1-5星） */
    private Integer score;

    /** 评价内容摘要（前100字） */
    private String contentSummary;

    /** 评价图片缩略图（最多3张，无图则为空） */
    private List<String> imageThumbs;

    /** 评价标签（最多显示2个） */
    private List<String> tags;

    /** 有用数 */
    private Integer helpfulCount;

    /** 评价时间 */
    private LocalDateTime createTime;

    /** 是否有追评（true/false） */
    private Boolean hasAdditional;

    /** 评价人极简信息内部类 */
    @Data
    public static class EvaluatorSimpleDTO {
        private String username; // 脱敏用户名
        private String avatarUrl;
    }
}
