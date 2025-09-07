package com.community_shop.backend.enums.simpleEnum;

public enum PayTypeEnum {
    WECHAT_PAY("微信支付"),
    ALIPAY("支付宝"),
    BANK_CARD("银行卡"),
    CASH("现金"),
    UNION_PAY("银联支付");

    private final String desc;

    PayTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
