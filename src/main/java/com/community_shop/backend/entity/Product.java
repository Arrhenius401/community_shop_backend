package com.community_shop.backend.entity;

import com.community_shop.backend.enums.codeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.codeEnum.ProductStatusEnum;
import lombok.Data;

@Data
public class Product {
    private Long productId; // 商品ID
    private Long sellerId; // 卖家ID
    private String title; // 标题
    private String category; // 类别（如“二手手机”）
    private Double price; // 价格
    private Integer stock; // 库存
    private Integer viewCount; // 浏览量
    private ProductStatusEnum status;   // 商品状态
    private ProductConditionEnum condition; // 成色（全新/9成新等）

    public Product(){}

    public Product(Long productId, Long sellerId, String title, String category, Double price, Integer stock, Integer viewCount, ProductStatusEnum status, ProductConditionEnum condition) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.viewCount = viewCount;
        this.status = status;
        this.condition = condition;
    }
}
