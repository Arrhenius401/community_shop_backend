package com.community_shop.backend.enums.CodeEnum;

public enum EvaluationStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    NORMAL("NORMAL", "正常展示"), // 评价通过校验，无举报/违规，正常显示
    REPORTED_PENDING("REPORTED_PENDING", "举报待审核"), // 用户发起举报，平台未完成审核（周期24小时）
    REPORTED_VALID("REPORTED_VALID", "举报成立"), // 审核后确认是虚假评价，标记为违规
    REPORTED_INVALID("REPORTED_INVALID", "举报不成立"), // 审核后确认评价真实，维持正常展示
    DELETED("DELETED", "已删除"); // 评价被用户/管理员删除（逻辑删除，保留数据）

    private final String code; // 用于数据库存储（varchar类型）
    private final String desc; // 用于前端展示、日志打印等

    // 构造器
    EvaluationStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法（无Setter，枚举值不可修改）
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 辅助方法：根据code反向获取枚举对象（用于MyBatis从数据库值转换为枚举）
    public static EvaluationStatusEnum getByCode(String code) {
        for (EvaluationStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的评价状态code：" + code);
    }
}
