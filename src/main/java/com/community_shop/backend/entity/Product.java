package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("product")
public class Product {

    /** 商品ID */
    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    /** 卖家ID */
    private Long sellerId;

    /** 商品标题 */
    private String title;

    /** 商品类别 */
    private String category;

    /** 商品描述 */
    private String description;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品库存 */
    private Integer stock;

    /** 浏览量 */
    private Integer viewCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 商品状态 */
    private ProductStatusEnum status;

    /** 商品成色 */
    private ProductConditionEnum condition;

    public Product(){}

}
