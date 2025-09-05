package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.OrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId; // 订单ID
    private Long productId; // 商品ID
    private Long buyerId; // 买家ID
    private Long sellerId; // 卖家ID
    private Double amount; // 交易金额
    private LocalDateTime createTime; // 下单时间
    private LocalDateTime payTime; // 支付时间
    private OrderStatusEnum status; // 订单状态

    public Order(){}

    public Order(Long orderId, Long productId, Long buyerId, Long sellerId, Double amount, LocalDateTime createTime, LocalDateTime payTime, OrderStatusEnum status) {
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.createTime = createTime;
        this.payTime = payTime;
        this.status = status;
    }
}
