package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举类
 */
@AllArgsConstructor
@Getter
public enum PayStatusEnum {

    /** 待支付 */
    PENDING("PENDING", "待支付"),

    /** 支付成功 */
    SUCCESS("SUCCESS", "支付成功"),

    /** 支付失败 */
    FAIL("FAIL", "支付失败"),

    /** 取消支付 */
    CANCELED("CANCELED", "取消支付"),

    /** 退款中 */
    REFUNDED("REFUNDED", "已退款");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static PayStatusEnum getByCode(String code) {
        for (PayStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
