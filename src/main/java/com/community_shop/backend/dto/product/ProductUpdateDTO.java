package com.community_shop.backend.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品信息更新请求DTO（匹配ProductService.updateProductInfo方法的ProductUpdateVO）
 */
@Data
public class ProductUpdateDTO {

    /** 商品ID（需更新的商品唯一标识） */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 商品标题（1-50位，可选更新） */
    @Size(max = 50, message = "商品标题不能超过50位")
    private String title;

    /** 商品类别（可选更新） */
    private String category;

    /** 商品单价（大于0，可选更新） */
    @Positive(message = "商品单价必须大于0")
    private Double price;

    /** 商品描述（1-1000位，可选更新） */
    @Size(max = 1000, message = "商品描述不能超过1000位")
    private String description;

    /** 图片URL列表（JSON格式，最多9张，可选更新） */
    private String imageUrls;

    /** 卖家ID（当前登录用户，校验权限） */
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;
}
