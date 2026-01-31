package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价排序字段枚举（匹配EvaluationQueryDTO的排序需求）
 */
@Getter
@AllArgsConstructor
public enum EvaluationSortFieldEnum {

    /** 评价时间 */
    CREATE_TIME("createTime", "create_time", "评价时间"),

    /** 评分 */
    SCORE("score", "score", "评分"),

    /** 有用数 */
    HELPFUL_COUNT("helpfulCount", "helpful_count", "有用数");


    /** 前端传入的字段名 */
    @EnumValue
    @JsonValue
    private final String fieldName;

    /** 数据库字段名 */
    private final String dbField;

    /** 描述 */
    private final String description;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
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
