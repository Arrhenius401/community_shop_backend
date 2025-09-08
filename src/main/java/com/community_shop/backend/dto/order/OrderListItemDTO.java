package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单列表项DTO（配合PageResult使用，适配列表页展示）
 * 依据：
 * - 列表页仅需展示订单核心信息，减少数据传输
 * - 数据库order表+order_item表的精简字段
 */
@Data
public class OrderListItemDTO {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 订单商品缩略信息（如“商品1等3件”） */
    private String productSummary;

    /** 商品首图（展示列表缩略图） */
    private String productImage;

    /** 订单总金额 */
    private Double totalAmount;

    /** 订单状态（带描述，如“待支付”） */
    private OrderStatusEnum status;

    /** 下单时间 */
    private LocalDateTime createTime;

    /** 支付时间（未支付则为null） */
    private LocalDateTime payTime;
}
