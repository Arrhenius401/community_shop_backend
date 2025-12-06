package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.code.ProductStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情响应DTO（匹配ProductService.selectProductById方法）
 */
@Data
@Schema(description = "商品详情响应数据")
public class ProductDetailDTO {

    /** 商品ID */
    @Schema(description = "商品ID", example = "2001")
    private Long productId;

    /** 卖家ID */
    @Schema(description = "卖家ID", example = "1001")
    private Long sellerId;

    /** 商品标题 */
    @Schema(description = "商品标题", example = "九成新iPhone 13")
    private String title;

    /** 商品类别 */
    @Schema(description = "商品类别", example = "二手手机")
    private String category;

    /** 商品单价 */
    @Schema(description = "商品单价（元）", example = "4999.99")
    private BigDecimal price;

    /** 剩余库存 */
    @Schema(description = "剩余库存数量", example = "5")
    private Integer stock;

    /** 商品描述（支持富文本格式） */
    @Schema(description = "商品描述（富文本）", example = "自用手机，无拆无修，电池健康90%")
    private String description;

    /** 图片URL列表（数组格式，适配前端展示） */
    @Schema(description = "商品图片URL数组", example = "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
    private String[] imageUrls;

    /** 浏览量 */
    @Schema(description = "商品浏览量", example = "120")
    private Integer viewCount;

    /** 发布时间 */
    @Schema(description = "商品发布时间", example = "2023-10-01T14:30:00")
    private LocalDateTime createTime;

    /** 商品状态 */
    @Schema(description = "商品状态", example = "ON_SALE")
    private ProductStatusEnum status;

    /** 商品成色 */
    @Schema(description = "商品成色", example = "九成新")
    private String condition;

}