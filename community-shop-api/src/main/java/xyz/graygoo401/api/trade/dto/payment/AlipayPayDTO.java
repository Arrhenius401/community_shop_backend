package xyz.graygoo401.api.trade.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付宝支付参数DTO
 */
@Data
public class AlipayPayDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId; // 关联现有订单表的orderId

}