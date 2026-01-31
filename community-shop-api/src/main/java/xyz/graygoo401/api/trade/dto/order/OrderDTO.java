package xyz.graygoo401.api.trade.dto.order;

import lombok.Data;
import xyz.graygoo401.api.trade.dto.product.ProductDTO;
import xyz.graygoo401.api.trade.enums.OrderStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long orderId;
    private String orderNo;

    // 数据聚合：不再仅仅是 Long ID
    private UserDTO buyer;    // 装配：Feign 远程调用 User 服务
    private UserDTO seller;   // 装配：Feign 远程调用 User 服务
    private ProductDTO product; // 装配：本服务内部 Mapper 查询或远程调用

    private BigDecimal totalAmount;
    private Integer quantity;
    private String receiverName;
    private String address;
    private String phoneNumber;
    private OrderStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}