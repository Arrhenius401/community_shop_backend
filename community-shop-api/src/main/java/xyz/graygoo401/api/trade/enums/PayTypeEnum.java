package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举类
 */
@AllArgsConstructor
@Getter
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

    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static PayTypeEnum getByCode(String code) {
        for (PayTypeEnum payType : values()) {
            if (payType.code.equals(code)) {
                return payType;
            }
        }
        return null;
    }
}
