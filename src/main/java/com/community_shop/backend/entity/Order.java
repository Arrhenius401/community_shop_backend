package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.OrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderID; // 订单ID
    private Long productID; // 商品ID
    private Long buyerID; // 买家ID
    private Long sellerID; // 卖家ID
    private Double amount; // 交易金额
    private LocalDateTime createTime; // 下单时间
    private LocalDateTime payTime; // 支付时间
    private OrderStatusEnum status; // 订单状态

    public Order(Long orderID, Long productID, Long buyerID, Long sellerID, Double amount, LocalDateTime createTime, LocalDateTime payTime, OrderStatusEnum status) {
        this.orderID = orderID;
        this.productID = productID;
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.amount = amount;
        this.createTime = createTime;
        this.payTime = payTime;
        this.status = status;
    }
}
