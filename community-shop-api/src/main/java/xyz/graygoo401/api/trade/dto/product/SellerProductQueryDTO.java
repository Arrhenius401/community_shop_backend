package xyz.graygoo401.api.trade.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 卖家商品列表查询DTO（适配ProductMapper.selectBySellerId接口）
 */
@Data
@Schema(description = "卖家商品列表查询参数")
public class SellerProductQueryDTO extends ProductQueryDTO {
    /** 最低库存（库存区间下限） */
    @Schema(description = "最低库存数量", example = "1")
    private Integer minStock;

    /** 最高库存（库存区间上限） */
    @Schema(description = "最高库存数量", example = "100")
    private Integer maxStock;
}