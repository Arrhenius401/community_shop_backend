package xyz.graygoo401.api.trade.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.graygoo401.api.trade.enums.ProductStatusEnum;

/**
 * 商品状态更新DTO
 */
@Data
@Schema(description = "商品状态更新参数")
public class ProductStatusUpdateDTO {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "2001")
    private Long productId;

    /** 商品状态 */
    @NotNull(message = "商品状态不能为空")
    @Schema(description = "商品状态（如上架/下架）", example = "ON_SALE")
    private ProductStatusEnum status;
}