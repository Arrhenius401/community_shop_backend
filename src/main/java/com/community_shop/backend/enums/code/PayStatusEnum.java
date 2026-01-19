package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 支付状态枚举类
 */
public enum PayStatusEnum {

    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAIL("FAIL", "支付失败"),
    CANCELED("CANCELED", "取消支付"),
    REFUNDED("REFUNDED", "已退款");

    @EnumValue
    @Getter
    private final String code;

    @Getter
    private final String desc;

    PayStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
