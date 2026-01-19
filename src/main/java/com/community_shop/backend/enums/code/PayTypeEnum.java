package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 支付方式枚举类
 */
public enum PayTypeEnum {
    /** 微信支付 */
    WECHAT_PAY("WECHAT_PAY", "微信支付"),

    /** 支付宝支付 */
    ALIPAY("ALIPAY", "支付宝支付"),

    /** 银行卡支付 */
    BANK_CARD("BANK_CARD", "银行卡支付"),

    /** 现金支付 */
    CASH("CASH", "现金支付"),

    /** 银联支付 */
    UNION_PAY("UNION_PAY", "银联支付");

    @Getter
    @EnumValue
    private final String code;

    @Getter
    private final String desc;

    PayTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
