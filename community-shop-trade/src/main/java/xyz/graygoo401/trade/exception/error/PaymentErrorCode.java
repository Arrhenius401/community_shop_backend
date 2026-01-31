package xyz.graygoo401.trade.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.graygoo401.common.exception.error.IErrorCode;

/**
 * 支付模块错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum PaymentErrorCode implements IErrorCode {

    // 支付模块
    PAYMENT_FAILED("PAY_001", 400, "支付失败"),

    PAYMENT_NOT_EXISTS("PAY_002", 404, "支付不存在"),

    PAYMENT_GENERATE_URL_FAILS("PAY_011", 500, "生成支付链接失败"),
    PAYMENT_CALLBACK_VALIDATE_FAILS("PAY_012", 400, "支付回调参数验证失败"),
    PAYMENT_CALLBACK_FAILS("PAY_013", 400, "支付回调失败"),

    PAYMENT_USER_NOT_MATCH("PAY_101", 400, "用户不匹配"),

    PAYMENT_ALIPAY_FAILS("PAY_102", 400, "支付宝支付失败");

    private final String code;
    private final int standardCode;
    private final String message;

}
