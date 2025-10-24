package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;


/**
 * 商品更新DTO类，用于封装商家更新商品信息的请求参数
 * 对应ProductService.updateProduct方法的入参
 */
@Data
public class ProductUpdateDTO {

    /**
     * 商品ID（必须存在，用于定位待更新商品）
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 商品标题（可选更新，长度1-100）
     */
    @Length(min = 1, max = 100, message = "商品标题长度必须在1-100之间")
    private String title;

    /**
     * 商品描述（可选更新，长度0-2000）
     */
    @Length(max = 2000, message = "商品描述不能超过2000字符")
    private String description;

    /**
     * 商品价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;

    /**
     * 商品库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    /**
     * 商品类别（可选更新，如"数码产品"、"服装鞋帽"等）
     */
    @Length(max = 50, message = "商品类别不能超过50字符")
    private String category;

    /**
     * 商品状态（可选更新，0-下架，1-上架）
     */
    @NotNull(message = "状态参数无效")
    private ProductStatusEnum status;

    /**
     * 商品成色
     */
    @NotNull(message = "成色不能为空")
    private ProductConditionEnum condition;

    /**
     * 商品主图URL（可选更新，更换主图时使用）
     */
    @Length(max = 255, message = "主图URL过长")
    private String mainImageUrl;

    /**
     * 商品详情图URL数组（可选更新，新增或替换详情图时使用）
     */
    private List<String> detailImageUrls;
}
