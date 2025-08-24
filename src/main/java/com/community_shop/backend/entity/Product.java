package com.community_shop.backend.entity;

import com.community_shop.backend.component.statusEnum.ProductStatusEnum;
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
}
