package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@AllArgsConstructor
@Getter
public enum OrderStatusEnum {

    /** 下单后未支付 */
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),

    /** 支付后等待卖家发货 */
    PENDING_SHIPMENT("PENDING_SHIPMENT", "待发货"),

    /** 卖家已发货（可关联物流轨迹） */
    SHIPPED("SHIPPED", "已发货"),

    /** 待收货（等待卖家收获） */
    PENDING_RECEIVE("PENDING_RECEIVE", "待收货"),

    /** 交易完成（买家确认收货） */
    COMPLETED("COMPLETED", "已完成"),

    /** 未支付时取消或协商取消 */
    CANCELLED("CANCELLED", "已取消"),

    /** 售后阶段，待退款流程 */
    REFUNDING("REFUNDING", "退款中"),

    /** 售后阶段，卖家退款完成 */
    REFUNDED("REFUNDED", "已退款"),

    /** 售后阶段，卖家退货完成 */
    RETURNED("RETURNED", "已退货"),

    /** 售后阶段，平台仲裁中 */
    ARBITRATION("ARBITRATION", "平台仲裁中");

    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static OrderStatusEnum getByCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
