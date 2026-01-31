package xyz.graygoo401.api.trade.enums;

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

    /** 前端传入的字段名 */
    @JsonValue
    private final String fieldName;

    /** 数据库字段名 */
    private final String dbField;

    /** 描述 */
    private final String description;

}
