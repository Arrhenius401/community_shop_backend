package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum MessageTypeEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    SYSTEM("SYSTEM", "系统消息"),
    ORDER("ORDER", "订单消息"),
    PRIVATE("PRIVATE", "私信消息");

    @EnumValue
    private final String code;
    @Getter
    private final String desc;

    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters
    // 在getCode()方法上添加@JsonValue注解，明确指定序列化时只输出 code 的值
    @JsonValue
    public String getCode() {
        return code;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的消息状态code：" + code);
    }
}
