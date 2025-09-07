package com.community_shop.backend.enums.codeEnum;

public enum MessageTypeEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    SYSTEM("SYSTEM", "系统消息"),
    ORDER("ORDER", "订单消息");

    private final String code;
    private final String desc;

    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters
    public String getDesc() {
        return desc;
    }

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
