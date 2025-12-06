package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.code.OrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单列表项DTO（配合PageResult使用，适配列表页展示）
 * 依据：
 * - 列表页仅需展示订单核心信息，减少数据传输
 * - 数据库order表+order_item表的精简字段
 */
@Schema(description = "订单列表项DTO，用于订单列表页展示核心信息")
@Data
public class OrderListItemDTO {

    /** 订单ID */
    @Schema(description = "订单ID", example = "10001")
    private Long orderId;

    /** 订单编号 */
    @Schema(description = "订单编号", example = "ORD20240520123456")
    private String orderNo;

    /** 订单商品缩略信息（如“商品1等3件”） */
    @Schema(description = "商品简要描述", example = "华为Mate 60 Pro等1件")
    private String productSummary;

    /** 商品首图（展示列表缩略图） */
    @Schema(description = "商品首图URL", example = "https://example.com/images/mate60.jpg")
    private String productImage;

    /** 订单总金额 */
    @Schema(description = "订单总金额", example = "6999.00")
    private Double totalAmount;

    /** 订单状态（带描述，如“待支付”） */
    @Schema(description = "订单状态（枚举）", example = "WAIT_PAY")
    private OrderStatusEnum status;

    /** 下单时间 */
    @Schema(description = "订单创建时间", example = "2024-05-20T10:00:00")
    private LocalDateTime createTime;

    /** 支付时间（未支付则为null） */
    @Schema(description = "支付时间，未支付则为null", example = "2024-05-20T14:30:00")
    private LocalDateTime payTime;
}