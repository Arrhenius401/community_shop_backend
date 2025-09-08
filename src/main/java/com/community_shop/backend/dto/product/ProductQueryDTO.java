package com.community_shop.backend.dto.product;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.SortEnum.ProductSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import lombok.Data;

/**
 * 商品搜索条件DTO（匹配ProductService.selectProductByKeyword及Mapper层selectByCondition）
 */
@Data
public class ProductQueryDTO extends PageParam {

    /** 搜索关键词（模糊匹配标题/描述） */
    private String keyword;

    /** 商品类别（精确筛选，如“二手手机”） */
    private String category;

    /** 最低价格（价格区间下限） */
    private Double minPrice;

    /** 最高价格（价格区间上限） */
    private Double maxPrice;

    /** 商品成色（精确筛选，如“全新”“9成新”） */
    private ProductConditionEnum condition;

    /** 排序字段（枚举：viewCount-浏览量；createTime-发布时间；price-价格） */
    private ProductSortFieldEnum sortField = ProductSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
