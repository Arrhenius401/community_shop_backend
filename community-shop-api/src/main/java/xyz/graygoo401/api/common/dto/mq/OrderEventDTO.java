package xyz.graygoo401.api.common.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDTO {
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private String orderNo;
    private String type;
}
