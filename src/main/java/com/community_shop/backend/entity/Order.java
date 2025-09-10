package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.dto.order.OrderCreateDTO;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@AllArgsConstructor
@Data
@TableName("`order`")
public class Order {

    /** 订单ID */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 交易金额 */
    private Double totalAmount;

    /** 订单数量 */
    private Integer quantity;

    /** 买家名称 */
    private String receiverName;

    /** 收货地址 */
    private String address;

    /** 买家留言 */
    private String buyerRemark;

    /** 订单状态 */
    private OrderStatusEnum status;

    /** 支付方式 */
    private PayTypeEnum payType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 收货时间 */
    private LocalDateTime receiveTime;


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

    public Order(OrderCreateDTO orderCreateDTO){
        this.productId = orderCreateDTO.getProductId();
        this.buyerId = orderCreateDTO.getBuyerId();
        this.quantity = orderCreateDTO.getQuantity();
        this.totalAmount = orderCreateDTO.getTotalAmount();
        this.address = orderCreateDTO.getAddress();
        this.buyerRemark = orderCreateDTO.getBuyerRemark();
        this.payType = orderCreateDTO.getPayType();

        this.status = OrderStatusEnum.PENDING_PAYMENT;
        this.createTime = LocalDateTime.now();
    }
}
