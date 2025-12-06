package com.community_shop.backend.enums.code;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum PostFollowStatusEnum {
    // code：数据库存储标识（适配varchar类型）；desc：状态描述（用于前端展示/业务逻辑说明）
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"), // 新用户发帖需审核时的状态
    NORMAL("NORMAL", "正常"),
    HIDDEN("HIDDEN", "隐藏"),
    BLOCKED("BLOCKED", "封禁"),
    DELETED("DELETED", "已删除"); // 逻辑删除状态

    @EnumValue
    private final String code; // 核心标识，用于MySQL varchar字段存储
    @Getter
    private final String desc; // 辅助说明，用于前端展示（如“该评论已被举报待审核”）、开发调试

    // 构造器（枚举值不可修改，无Setter方法）
    PostFollowStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法：提供code和desc的读取能力，适配MyBatis转换与前端展示
    // 在getCode()方法上添加@JsonValue注解，明确指定序列化时只输出 code 的值
    @JsonValue
    public String getCode() {
        return code;
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
