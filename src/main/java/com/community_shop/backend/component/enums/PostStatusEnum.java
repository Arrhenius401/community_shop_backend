package com.community_shop.backend.component.enums;


public enum PostStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    DRAFT("DRAFT", "草稿"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"), // 新用户发帖需审核时的状态
    PASSED("PASSED", "审核通过"),
    REJECTED("REJECTED", "审核驳回"),
    TOP("TOP", "置顶"), // 吧主/管理员设置的置顶状态（最多5篇）
    ESSENCE("ESSENCE", "精华帖"), // 吧主标记的优质内容
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
