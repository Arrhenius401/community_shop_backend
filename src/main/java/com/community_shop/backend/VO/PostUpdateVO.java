package com.community_shop.backend.VO;

import lombok.Data;

@Data
public class PostUpdateVO {
    // 商品标题（可选，若不为空则长度1-100）
    private String title;

    // 商品价格（可选，若不为空则≥0.01）
    private Double price;

    // 商品库存（可选，若不为空则≥0）
    private Integer stock;

    // 商品描述（可选，若不为空则长度1-5000）
    private String description;
//
//    // 图片列表（可选，若不为空则至少1张，最多5张）
//    private List<String> imageUrls;
//
//    // 商品标签（可选，若不为空则最多5个）
//    private List<String> tags;
}
