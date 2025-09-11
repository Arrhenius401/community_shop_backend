package com.community_shop.backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单发货信息dto
 */
@Data
public class OrderShipDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 快递公司 */
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    /** 快递单号 */
    @NotBlank(message = "物流单号不能为空")
    private String expressNo;

    /** 发货备注 */
    private String shipRemark;
}
