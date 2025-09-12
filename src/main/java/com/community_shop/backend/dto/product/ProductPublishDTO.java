package com.community_shop.backend.dto.product;

import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 商品发布请求DTO（匹配ProductService.publishProduct方法的ProductCreateVO）
 */
@Data
public class ProductPublishDTO {

    /** 卖家ID（当前登录用户，用于校验权限） */
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /** 商品标题（1-50位） */
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 50, message = "商品标题不能超过50位")
    private String title;

    /** 商品类别（如“二手手机”“家居用品”，非空） */
    @NotBlank(message = "商品类别不能为空")
    private String category;

    /** 商品单价（大于0，保留2位小数） */
    @NotNull(message = "商品单价不能为空")
    @Positive(message = "商品单价必须大于0")
    private Double price;

    /** 库存数量（大于等于1） */
    @NotNull(message = "库存数量不能为空")
    @Positive(message = "库存数量必须大于0")
    private Integer stock;

    /** 商品描述（1-1000位，支持富文本） */
    @NotBlank(message = "商品描述不能为空")
    @Size(max = 1000, message = "商品描述不能超过1000位")
    private String description;

    /** 图片URL列表（JSON格式，最多9张，匹配系统设计“多图上传”要求） */
    private List<String> imageUrls;

    /** 商品成色（枚举：全新/9成新/8成新/7成新/其他，需合法校验） */
    @NotNull(message = "商品成色不能为空")
    private ProductConditionEnum condition;

}
