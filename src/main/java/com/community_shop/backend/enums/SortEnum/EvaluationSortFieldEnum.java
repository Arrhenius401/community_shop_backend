package com.community_shop.backend.enums.SortEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价排序字段枚举（匹配EvaluationQueryDTO的排序需求）
 */
@Getter
@AllArgsConstructor
public enum EvaluationSortFieldEnum {
    CREATE_TIME("createTime", "create_time", "评价时间"),
    SCORE("score", "score", "评分"),
    HELPFUL_COUNT("helpfulCount", "helpful_count", "有用数");

    @JsonValue
    private final String fieldName; // 前端传入的字段名
    private final String dbField;   // 对应的数据库字段名
    private final String description;

    /**
     * 根据前端字段名匹配枚举
     */
    public static EvaluationSortFieldEnum getByFieldName(String fieldName) {
        for (EvaluationSortFieldEnum enumItem : values()) {
            if (enumItem.fieldName.equals(fieldName)) {
                return enumItem;
            }
        }
        return null;
    }
}
