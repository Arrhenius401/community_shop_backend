package com.community_shop.backend.enums.SimpleEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    /** 微信支付 */
    WECHAT_PAY("WECHAT_PAY"),

    /** 支付宝支付 */
    ALIPAY("ALIPAY"),

    /** 银行卡支付 */
    BANK_CARD("BANK_CARD"),

    /** 现金支付 */
    CASH("CASH"),

    /** 银联支付 */
    UNION_PAY("UNION_PAY");

    private final String code;

}
