package com.community_shop.backend.entity.enums.statusEnum;

public enum ProductStatusEnum {

    PENDING_REVIEW("PENDING_REVIEW", "待审核"), // 新发布商品（尤其新用户发布），需管理员预审（参考帖子预审逻辑）
    ON_SALE("ON_SALE", "在售"), // 审核通过，库存＞0，可被搜索/购买
    SOLD_OUT("SOLD_OUT", "已售罄"), // 库存=0，不可购买，仅保留展示（用户可查看历史商品）
    OFF_SHELF_TEMP("OFF_SHELF_TEMP", "临时下架"), // 卖家主动下架（如调整商品信息），可重新上架
    OFF_SHELF_PERMANENT("OFF_SHELF_PERMANENT", "永久下架"), // 违规商品（如虚假描述），被管理员强制下架，不可重新上架
    DELETED("DELETED", "已删除"); // 卖家删除商品（逻辑删除，保留交易关联数据）

    private final String code; // 用于数据库存储（varchar类型）
    private final String desc; // 用于前端展示、商品列表筛选

    // 构造器
    ProductStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
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
