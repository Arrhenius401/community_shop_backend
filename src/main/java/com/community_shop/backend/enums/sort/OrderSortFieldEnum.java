package com.community_shop.backend.enums.sort;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单排序字段枚举（匹配OrderQueryDTO）
 */
@Getter
@AllArgsConstructor
public enum OrderSortFieldEnum {

    CREATE_TIME("createTime", "create_time", "创建时间"),
    PAY_TIME("payTime", "pay_time", "支付时间"),
    TOTAL_AMOUNT("totalAmount", "total_amount", "订单金额");

    @JsonValue
    private final String fieldName; // 前端传入的字段名
    private final String dbField;   // 数据库字段名（用于SQL排序）
    private final String description;

    /**
     * 根据前端字段名匹配枚举
     */
    public static OrderSortFieldEnum getByFieldName(String fieldName) {
        for (OrderSortFieldEnum enumItem : values()) {
            if (enumItem.fieldName.equals(fieldName)) {
                return enumItem;
            }
        }
        return null;
    }
}
