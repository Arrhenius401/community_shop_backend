package com.community_shop.backend.dto.payment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AlipayPayDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId; // 关联现有订单表的orderId

}