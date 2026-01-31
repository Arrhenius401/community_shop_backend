package xyz.graygoo401.trade.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 订单模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum OrderErrorCode implements IErrorCode {

    // 订单模块
    ORDER_NOT_EXISTS("ORDER_001", 404, "订单不存在"),
    ORDER_ID_NULL("ORDER_002", 400, "订单ID为空"),
    ORDER_AMOUNT_NULL("ORDER_003", 400, "订单金额为空"),
    ORDER_STATUS_NULL("ORDER_004", 400, "订单状态为空"),
    ORDER_BUYER_NULL("ORDER_005", 400, "订单买家为空"),
    ORDER_SELLER_NULL("ORDER_006", 400, "订单卖家为空"),

    ORDER_STATUS_INVALID("ORDER_004", 400, "订单状态参数错误"),

    ORDER_AMOUNT_ABNORMAL("ORDER_007", 400, "订单金额参数错误"),

    ORDER_NOT_COMPLETED("ORDER_005", 400, "订单未完成");

    private final String code;
    private final int standardCode;
    private final String message;

}
