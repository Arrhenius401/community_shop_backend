package com.community_shop.backend.dto.product;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.code.ProductConditionEnum;
import com.community_shop.backend.enums.code.ProductStatusEnum;
import com.community_shop.backend.enums.sort.ProductSortFieldEnum;
import com.community_shop.backend.enums.sort.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索条件DTO（匹配ProductService.selectProductByKeyword及Mapper层selectByCondition）
 */
@Data
@Schema(description = "商品搜索查询参数")
public class ProductQueryDTO extends PageParam {

    /** 卖家ID（当前登录卖家） */
    @NotNull(message = "卖家ID不能为空")
    @Schema(description = "卖家ID（当前登录用户ID）", example = "1001")
    private Long sellerId;

    /** 搜索关键词（模糊匹配标题/描述） */
    @Schema(description = "搜索关键词（模糊匹配标题/描述）", example = "iPhone")
    private String keyword;

    /** 商品类别（精确筛选，如“二手手机”） */
    @Schema(description = "商品类别（精确筛选）", example = "二手手机")
    private String category;

    /** 最低价格（价格区间下限） */
    @Schema(description = "最低价格（元）", example = "3000")
    private BigDecimal minPrice;

    /** 最高价格（价格区间上限） */
    @Schema(description = "最高价格（元）", example = "6000")
    private BigDecimal maxPrice;

    /** 商品成色（精确筛选，如“全新”“9成新”） */
    @Schema(description = "商品成色（精确筛选）", example = "NINE_NEW")
    private ProductConditionEnum condition;

    /** 商品状态（精确筛选，如“在售”） */
    @Schema(description = "商品状态（精确筛选）", example = "ON_SALE")
    private ProductStatusEnum status;

    /** 排序字段（枚举：viewCount-浏览量；createTime-发布时间；price-价格） */
    @Schema(description = "排序字段", example = "CREATE_TIME", defaultValue = "CREATE_TIME")
    private ProductSortFieldEnum sortField = ProductSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    @Schema(description = "排序方向", example = "DESC", defaultValue = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}