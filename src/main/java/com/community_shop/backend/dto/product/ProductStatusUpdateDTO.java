package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品状态更新DTO
 */
@Data
public class ProductStatusUpdateDTO {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 商品状态 */
    @NotNull(message = "商品状态不能为空")
    private ProductStatusEnum status;
}
