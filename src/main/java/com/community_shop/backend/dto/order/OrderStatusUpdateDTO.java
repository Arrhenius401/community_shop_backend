package com.community_shop.backend.dto.order;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单状态更新请求DTO（匹配OrderService.updateOrderStatus方法）
 * 依据：
 * - 数据库order表：status字段状态流转规则
 * - Service层规则：状态变更需校验权限（买家/卖家/系统）、验证状态合法性
 */
@Schema(description = "订单状态更新请求DTO，用于变更订单状态")
@Data
public class OrderStatusUpdateDTO {

    /** 订单ID（非空） */
    @Schema(description = "订单ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 目标状态（枚举值，非空） */
    @Schema(description = "目标状态（枚举字符串）", example = "SHIPPED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标状态不能为空")
    private OrderStatusEnum targetStatus;

    /** 操作人ID（非空，用于权限校验） */
    @Schema(description = "操作人用户ID（用于权限校验）", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /** 附加信息（如支付流水号、物流单号，可选） */
    @Schema(description = "附加信息（如物流单号、支付流水号）", example = "SF1234567890123")
    private String extraInfo;
}