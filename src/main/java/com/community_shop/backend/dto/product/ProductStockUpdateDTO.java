package com.community_shop.backend.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存调整请求DTO（匹配ProductService.updateStock方法）
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "商品库存调整参数")
public class ProductStockUpdateDTO {

    /** 商品ID（需调整库存的商品） */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "2001")
    private Long productId;

    /** 库存变更值（正数增加，负数减少；扣减时需校验库存充足） */
    @NotNull(message = "库存变更值不能为空")
    @Schema(description = "库存变更值（正数增加，负数减少）", example = "10")
    private Integer stockChange;

    /** 调整原因（如“补货”“订单扣减”“库存盘点修正”，必填） */
    @Schema(description = "库存调整原因", example = "补货")
    private String reason;

}