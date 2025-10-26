package com.community_shop.backend.dto.product;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 卖家商品列表查询DTO（适配ProductMapper.selectBySellerId接口）
 */
@Data
public class SellerProductQueryDTO extends ProductQueryDTO {
    /** 最低库存（库存区间下限） */
    private Integer minStock;

    /** 最高库存（库存区间上限） */
    private Integer maxStock;
}
