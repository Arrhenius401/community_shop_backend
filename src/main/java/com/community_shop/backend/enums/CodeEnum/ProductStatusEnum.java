package com.community_shop.backend.enums.CodeEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ProductStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    PENDING("PENDING", "待审核"), // 新发布商品（尤其新用户发布），需管理员预审（参考帖子预审逻辑）
    ON_SALE("ON_SALE", "在售"), // 审核通过，库存＞0，可被搜索/购买
    SOLD_OUT("SOLD_OUT", "已售罄"), // 库存=0，不可购买，仅保留展示（用户可查看历史商品）
    OFF_SHELF("SHELF_TEMP", "已下架"), // 卖家主动下架（如调整商品信息），可重新上架
    HIDDEN("HIDDEN", "隐藏"), // 卖家隐藏商品（仅自己可见），可重新上架
    BLOCKED("BLOCKED", "封禁"),   // 卖家违规，被封禁
    DELETED("DELETED", "已删除"); // 卖家删除商品（逻辑删除，保留交易关联数据）

    private final String code; // 用于数据库存储（varchar类型）
    @Getter
    private final String desc; // 用于前端展示、商品列表筛选

    // 构造器
    ProductStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法
    // 在getCode()方法上添加@JsonValue注解，明确指定序列化时只输出 code 的值
    @JsonValue
    public String getCode() {
        return code;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static ProductStatusEnum getByCode(String code) {
        for (ProductStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的商品状态code：" + code);
    }
}
