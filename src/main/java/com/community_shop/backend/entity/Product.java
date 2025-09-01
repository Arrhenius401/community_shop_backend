package com.community_shop.backend.entity;

import com.community_shop.backend.component.enums.ProductStatusEnum;
import lombok.Data;

@Data
public class Product {
    private Long productID; // 商品ID
    private Long sellerId; // 卖家ID
    private String title; // 标题
    private String category; // 类别（如“二手手机”）
    private String condition; // 成色（全新/9成新等）
    private Double price; // 价格
    private Integer stock; // 库存
    private Integer viewCount; // 浏览量
    private ProductStatusEnum status;   // 商品状态


    public Product(Long productID, Long sellerId, String title, String category, String condition, Double price, Integer stock, Integer viewCount, ProductStatusEnum status) {
        this.productID = productID;
        this.sellerId = sellerId;
        this.title = title;
        this.category = category;
        this.condition = condition;
        this.price = price;
        this.stock = stock;
        this.viewCount = viewCount;
        this.status = status;
    }
}
