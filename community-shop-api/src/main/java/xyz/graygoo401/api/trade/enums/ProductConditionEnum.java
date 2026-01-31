package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品成色枚举类
 */
@AllArgsConstructor
@Getter
public enum ProductConditionEnum {

    /** 未拆封/未使用 */
    NEW("NEW", "全新"),

    /** 轻微使用痕迹，功能完好 */
    NINETY_FIVE_PERCENT_NEW("NINETY_FIVE_PERCENT_NEW", "95成新"),

    /** 明显使用痕迹，无损坏 */
    NINETY_PERCENT_NEW("NINETY_PERCENT_NEW", "9成新"),

    /** 有较多使用痕迹，功能正常 */
    EIGHTY_PERCENT_NEW("EIGHTY_PERCENT_NEW", "8成新"),

    /** 有使用痕迹，功能正常 */
    SEVENTY_PERCENT_NEW("SEVENTY_PERCENT_NEW", "7成新"),

    /** 长期使用，无核心故障 */
    USED("USED", "闲置");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static ProductConditionEnum getByCode(String code) {
        for (ProductConditionEnum condition : values()) {
            if (condition.code.equals(code)) {
                return condition;
            }
        }
        return null;
    }
}
