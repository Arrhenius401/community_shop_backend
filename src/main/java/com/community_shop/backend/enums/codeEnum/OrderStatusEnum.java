package com.community_shop.backend.enums.codeEnum;

public enum OrderStatusEnum {
    // code：数据库存储标识；desc：状态描述（用于前端展示/开发理解）
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"), // 下单后未支付
    PAID("PAID", "已支付"), // 用户完成支付
    PENDING_SHIPMENT("PENDING_SHIPMENT", "待发货"), // 支付后等待卖家发货
    SHIPPED("SHIPPED", "已发货"), // 卖家已发货（可关联物流轨迹）
    PENDING_RECEIPT("PENDING_RECEIPT", "待收货"), // 商品在途，等待买家确认
    COMPLETED("COMPLETED", "已完成"), // 买家确认收货，交易结束
    CANCELLED("CANCELLED", "已取消"), // 未支付时取消或协商取消
    REFUNDING("REFUNDING", "退款中"), // 售后阶段的退款流程
    REFUNDED("REFUNDED", "已退款"), // 退款完成
    RETURNED("RETURNED", "已退货"),
    ARBITRATION("ARBITRATION", "平台仲裁中"); // 卖家超时未响应售后时的状态

    private final String code;
    private final String desc;

    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters
    public String getCode() { return code; }
    public String getDesc() { return desc; }

    // 辅助方法：根据code反向获取枚举对象
    public static OrderStatusEnum getByCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态code：" + code);
    }
}
