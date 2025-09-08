package com.community_shop.backend.dto.product;

import lombok.Data;

@Data
public class ProductCreateVO {
    // 商品标题（非空，长度1-100）
    private String title;

    // 商品类别（非空，必须是系统支持的类别，如"数码产品"/"服装鞋帽"）
    private String category;

    // 商品价格（非空，≥0.01）
    private Double price;

    // 商品库存（非空，≥1）
    private Integer stock;

    // 商品成色（非空，必须是ProductConditionEnum枚举值："全新"/"9成新"/"8成新"/"7成新及以下"）
    private String condition;

    // 商品描述（非空，长度1-5000）
    private String description;

//    // 图片列表（非空，至少1张主图，最多5张，阿里云OSS存储）
//    private List<String> imageUrls;
//
//    // 视频URL（可选，最多1个视频，用于展示商品细节）
//    private String videoUrl;
//
//    // 商品标签（可选，最多5个，用于搜索和分类）
//    private List<String> tags;
}
