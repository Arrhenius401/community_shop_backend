package com.community_shop.backend.component.enums;

public enum MessageStatusEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    // 未读状态：用户尚未查看该消息
    UNREAD("UNREAD", "未读"),
    // 已读状态：用户已查看该消息
    READ("READ", "已读"),
    // 已删除状态：用户删除了该消息（逻辑删除标记）
    DELETED("DELETED", "已删除");

    private final String code;
    private final String desc;

    MessageStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static MessageStatusEnum getByCode(String code) {
        for (MessageStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的消息状态code：" + code);
    }
}
