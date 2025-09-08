package com.community_shop.backend.dto.product;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 卖家商品列表查询DTO（适配ProductMapper.selectBySellerId接口）
 */
@Data
public class SellerProductQueryDTO extends PageParam {

    /** 卖家ID（当前登录卖家） */
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /** 商品状态（可选筛选：ON_SALE 在售） */
    private ProductStatusEnum status = ProductStatusEnum.ON_SALE;

    /** 排序字段（枚举：createTime-发布时间；stock-库存；viewCount-浏览量） */
    private String sortField = "createTime";

    /** 排序方向（asc-升序；desc-降序） */
    private String sortDir = "desc";
}
