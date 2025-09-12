package com.community_shop.backend.convert;

import com.community_shop.backend.dto.product.*;
import com.community_shop.backend.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Product 模块对象转换器
 * 基于 MapStruct 实现实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring", uses = ObjectMapper.class) // 引入 ObjectMapper 处理 JSON 字符串与数组转换
public interface ProductConvert {

    // 单例实例（非 Spring 环境使用）
    ProductConvert INSTANCE = Mappers.getMapper(ProductConvert.class);

    /**
     * Product 实体 -> ProductDetailDTO（商品详情响应）
     * 映射说明：
     * 1. 枚举类型因类型一致可自动映射
     */
//    @Mapping(target = "imageUrls", expression = "java(jsonToList(product.getImageUrls()))")
    ProductDetailDTO productToProductDetailDTO(Product product);

    /**
     * Product 实体 -> ProductListItemDTO（商品列表项响应）
     * 映射说明：
     * 1. 枚举类型因类型一致可自动映射
     */
    ProductListItemDTO productToProductListItemDTO(Product product);

    /**
     * ProductPublishDTO（商品发布请求）-> Product 实体
     * 映射说明：
     * 1. 发布时默认初始化浏览量 0、状态为在售
     * 2. 忽略实体中自动生成的字段
     */
    @Mappings({
            @Mapping(target = "productId", ignore = true), // 主键自增
            @Mapping(target = "viewCount", constant = "0"), // 初始浏览量 0
            @Mapping(target = "createTime", ignore = true), // 发布时间由系统生成
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.ProductStatusEnum.ON_SALE)"), // 默认在售
            @Mapping(target = "imageUrls", source = "imageUrls") // 直接接收 JSON 字符串，存储到实体
    })
    Product productPublishDtoToProduct(ProductPublishDTO dto);

    /**
     * ProductCreateVO（商品创建 VO）-> Product 实体
     * 映射说明：适配旧版创建接口，逻辑与发布接口类似
     */
    @Mappings({
            @Mapping(target = "productId", ignore = true),
            @Mapping(target = "sellerId", ignore = true), // 由上下文传入，不依赖 VO
            @Mapping(target = "viewCount", constant = "0"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.ProductStatusEnum.ON_SALE)"),
            @Mapping(target = "imageUrls", expression = "java(listToJson(vo.getImageUrls()))") // 将 List 转为 JSON 字符串存储
    })
    Product productCreateVoToProduct(ProductCreateVO vo);

    /**
     * ProductUpdateDTO（商品更新请求）-> Product 实体
     * 映射说明：仅更新请求中携带的非空字段，忽略不可修改的字段
     */
    @Mappings({
            @Mapping(target = "productId", source = "productId"), // 仅用于定位商品，不修改
            @Mapping(target = "sellerId", ignore = true), // 卖家 ID 不可修改
            @Mapping(target = "viewCount", ignore = true), // 浏览量由系统维护
            @Mapping(target = "createTime", ignore = true), // 创建时间不可修改
            @Mapping(target = "imageUrls", ignore = true) // 图片更新需单独处理（如需替换需重新上传）
    })
    void updateProductFromUpdateDto(ProductUpdateDTO dto, @MappingTarget Product product);

    /**
     * 批量转换 Product 列表 -> ProductDetailDTO 列表
     */
    List<ProductDetailDTO> productListToProductDetailList(List<Product> products);

    /**
     * 辅助方法：JSON 字符串转 String 数组
     * @param json JSON 格式的 URL 列表（如 "[\"url1\",\"url2\"]"）
     * @return String 数组，转换失败返回空数组
     */
    default String[] jsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return new String[0];
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, String[].class);
        } catch (JsonProcessingException e) {
            return new String[0];
        }
    }

    /**
     * 辅助方法：List<String> 转 JSON 字符串
     * @param list URL 列表
     * @return JSON 字符串，转换失败返回空字符串
     */
    default String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
