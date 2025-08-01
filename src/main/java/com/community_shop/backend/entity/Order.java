package com.community_shop.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId; // 订单ID
    private Long productId; // 商品ID
    private Long buyerId; // 买家ID
    private Long sellerId; // 卖家ID
    private Double amount; // 交易金额
    private String status; // 状态（枚举值）
    private LocalDateTime createTime; // 下单时间
    private LocalDateTime payTime; // 支付时间

}
