package com.community_shop.backend.entity;

import com.community_shop.backend.VO.OrderCreateVO;
import com.community_shop.backend.component.enums.codeEnum.OrderStatusEnum;
import com.community_shop.backend.component.enums.simpleEnum.PayTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId; // 订单ID
    private Long productId; // 商品ID
    private Long buyerId; // 买家ID
    private Long sellerId; // 卖家ID
    private Integer quantity;   // 商品数量
    private Double totalAmount; // 交易金额
    private String address; // 收货地址
    private String buyerRemark; // 买家留言
    private LocalDateTime createTime; // 下单时间
    private LocalDateTime payTime; // 支付时间
    private OrderStatusEnum status; // 订单状态
    private PayTypeEnum payType;     // 支付方式

    public Order(){}

    public Order(Long orderId, Long productId, Long buyerId, Long sellerId, Integer quantity,
                 Double totalAmount, String address, LocalDateTime createTime, LocalDateTime payTime, OrderStatusEnum status) {
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.address = address;
        this.createTime = createTime;
        this.payTime = payTime;
        this.status = status;
    }

    public Order(OrderCreateVO orderCreateVO){
        this.productId = orderCreateVO.getProductId();
        this.buyerId = orderCreateVO.getBuyerId();
        this.quantity = orderCreateVO.getQuantity();
        this.totalAmount = orderCreateVO.getTotalAmount();
        this.address = orderCreateVO.getAddress();
        this.buyerRemark = orderCreateVO.getBuyerRemark();
        this.payType = orderCreateVO.getPayType();

        this.status = OrderStatusEnum.PENDING_PAYMENT;
        this.createTime = LocalDateTime.now();
    }
}
