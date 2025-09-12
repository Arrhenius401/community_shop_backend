package com.community_shop.backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 库存调整请求DTO（匹配ProductService.updateStock方法）
 */
@AllArgsConstructor
@Data
public class ProductStockUpdateDTO {

    /** 商品ID（需调整库存的商品） */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 库存变更值（正数增加，负数减少；扣减时需校验库存充足） */
    @NotNull(message = "库存变更值不能为空")
    private Integer stockChange;

    /** 调整原因（如“补货”“订单扣减”“库存盘点修正”，必填） */
    @NotBlank(message = "调整原因不能为空")
    private String reason;

}
