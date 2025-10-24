package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情响应DTO（匹配ProductService.selectProductById方法）
 */
@Data
public class ProductDetailDTO {

    /** 商品ID */
    private Long productId;

    /** 卖家ID */
    private Long sellerId;

    /** 商品标题 */
    private String title;

    /** 商品类别 */
    private String category;

    /** 商品单价 */
    private BigDecimal price;

    /** 剩余库存 */
    private Integer stock;

    /** 商品描述（支持富文本格式） */
    private String description;

    /** 图片URL列表（数组格式，适配前端展示） */
    private String[] imageUrls;

    /** 浏览量 */
    private Integer viewCount;

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 商品状态 */
    private ProductStatusEnum status;

    /** 商品成色 */
    private String condition;

}
