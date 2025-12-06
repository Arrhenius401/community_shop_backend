package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum EvaluationStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"), // 新用户发帖需审核时的状态
    NORMAL("NORMAL", "正常"),
    HIDDEN("HIDDEN", "隐藏"),
    BLOCKED("BLOCKED", "封禁"),
    DELETED("DELETED", "已删除"); // 逻辑删除状态

    @EnumValue
    private final String code; // 用于数据库存储（varchar类型）
    @Getter
    private final String desc; // 用于前端展示、日志打印等

    // 构造器
    EvaluationStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法（无Setter，枚举值不可修改）
    // 在getCode()方法上添加@JsonValue注解，明确指定序列化时只输出 code 的值
    @JsonValue
    public String getCode() {
        return code;
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
