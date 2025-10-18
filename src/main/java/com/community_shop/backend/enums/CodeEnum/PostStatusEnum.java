package com.community_shop.backend.enums.CodeEnum;


public enum PostStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"), // 新用户发帖需审核时的状态
    NORMAL("NORMAL", "正常"),
    HIDDEN("HIDDEN", "隐藏"),
    BLOCKED("BLOCKED", "封禁"),
    DELETED("DELETED", "已删除"); // 逻辑删除状态

    private final String code;
    private final String desc;

    PostStatusEnum(String code, String desc) {
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
    public static PostStatusEnum getByCode(String code) {
        for (PostStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的帖子状态code：" + code);
    }
}
