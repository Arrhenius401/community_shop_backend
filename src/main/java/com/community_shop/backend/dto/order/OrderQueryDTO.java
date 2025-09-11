package com.community_shop.backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单状态更新请求DTO（匹配OrderService.updateOrderStatus方法）
 * 依据：
 * - 数据库order表：status字段状态流转规则
 * - Service层规则：状态变更需校验权限（买家/卖家/系统）、验证状态合法性
 */
@Data
public class OrderQueryDTO {

    /** 订单ID（非空） */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 目标状态（枚举值，非空） */
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    /** 操作人类型（枚举：BUYER-买家；SELLER-卖家；SYSTEM-系统，非空） */
    @NotBlank(message = "操作人类型不能为空")
    private String operatorType;

    /** 附加信息（如支付流水号、物流单号，可选） */
    private String extraInfo;
}
