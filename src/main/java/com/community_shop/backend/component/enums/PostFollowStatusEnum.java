package com.community_shop.backend.component.enums;

public enum PostFollowStatusEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    NORMAL("NORMAL", "正常展示"), // 跟帖内容合规，无举报/违规，在帖子详情页正常显示（支持用户查看、互动）
    REPORTED_PENDING("REPORTED_PENDING", "举报待审核"), // 用户发起跟帖举报（参考评价举报逻辑），平台未完成审核（可沿用24小时审核周期）
    REPORTED_VALID("REPORTED_VALID", "举报成立-隐藏"), // 审核确认跟帖违规（如含不良信息），管理员标记后隐藏，普通用户不可见
    REPORTED_INVALID("REPORTED_INVALID", "举报不成立-恢复展示"), // 审核确认跟帖合规，维持/恢复正常展示状态
    ADMIN_HIDDEN("ADMIN_HIDDEN", "管理员手动隐藏"), // 管理员直接发现违规跟帖（未触发用户举报），手动隐藏（如广告、恶意评论）
    DELETED("DELETED", "用户/管理员删除"); // 跟帖发布者主动删除，或管理员强制删除（逻辑删除，保留数据用于违规统计）

    private final String code; // 核心标识，用于MySQL varchar字段存储
    private final String desc; // 辅助说明，用于前端展示（如“该评论已被举报待审核”）、开发调试

    // 构造器（枚举值不可修改，无Setter方法）
    PostFollowStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法：提供code和desc的读取能力，适配MyBatis转换与前端展示
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 辅助方法：根据数据库存储的code反向获取枚举对象（支撑MyBatis类型转换）
    public static PostFollowStatusEnum getByCode(String code) {
        for (PostFollowStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的跟帖状态code：" + code); // 避免非法值传入，保证类型安全
    }
}
