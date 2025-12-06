package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.code.ProductConditionEnum;
import com.community_shop.backend.enums.code.ProductStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表项DTO
 */
@Data
@Schema(description = "商品列表项数据")
public class ProductListItemDTO {

    /** 商品标题（非空，长度1-50） */
    @NotBlank(message = "商品标题不能为空")
    @Schema(description = "商品标题", example = "九成新iPhone 13")
    private String title;

    /** 商品类别（非空，长度1-50） */
    @NotBlank(message = "商品类别不能为空")
    @Schema(description = "商品类别", example = "二手手机")
    private String category;

    /** 商品单价（非空，必须大于0） */
    @NotNull(message = "商品单价不能为空")
    @Positive(message = "商品单价必须大于0")
    @Schema(description = "商品单价（元）", example = "4999.99")
    private BigDecimal price;

    /** 库存数量（非空，必须大于0） */
    @NotNull(message = "库存数量不能为空")
    @Positive(message = "库存数量必须大于0")
    @Schema(description = "库存数量", example = "5")
    private Integer stock;

    /** 商品描述（非空，长度1-1000） */
    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", example = "自用手机，无拆无修")
    private String description;

    /** 商品成色（非空） */
    @NotNull(message = "商品成色不能为空")
    @Schema(description = "商品成色", example = "NINE_NEW")
    private ProductConditionEnum condition;

    /** 商品状态 */
    @NotNull(message = "商品状态不能为空")
    @Schema(description = "商品状态", example = "ON_SALE")
    private ProductStatusEnum status;
}