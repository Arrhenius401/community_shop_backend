package com.community_shop.backend.convert;

import com.community_shop.backend.dto.product.ProductDetailDTO;
import com.community_shop.backend.dto.product.ProductPublishDTO;
import com.community_shop.backend.dto.product.ProductUpdateDTO;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.enums.CodeEnum.ProductConditionEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Product 模块 Entity 与 DTO 转换接口
 */
@Mapper(componentModel = "spring")
public interface ProductConvert {

    // 单例实例（非Spring环境使用）
    ProductConvert INSTANCE = Mappers.getMapper(ProductConvert.class);

    // 时间格式化器
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 提取JSON数组中URL的正则（适配"[\"url1\",\"url2\"]"格式）
    Pattern URL_EXTRACT_PATTERN = Pattern.compile("\"([^\"]+)\"");


    /**
     * ProductPublishDTO → ProductEntity（发布场景：请求参数转数据库实体）
     * 适配文档：
     * - imageUrls（JSON字符串）直接存储到product表的image_urls字段
     * - 初始化view_count=0、status=ON_SALE
     * - ProductConditionEnum转字符串存储
     */
    @Mapping(target = "productId", ignore = true) // 自增ID忽略
    @Mapping(target = "viewCount", constant = "0") // 初始浏览量0
    @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.ProductStatusEnum.ON_SALE.name())") // 初始在售
    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "validateJsonFormat") // 校验JSON格式
    @Mapping(target = "condition", source = "condition", qualifiedByName = "conditionEnumToString") // 成色枚举转字符串
    @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now())") // 发布时间
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now())") // 更新时间初始化
    Product publishDtoToEntity(ProductPublishDTO publishDTO);


    /**
     * ProductEntity → ProductDetailDTO（详情场景：数据库实体转响应DTO）
     * 适配文档：
     * - image_urls（JSON字符串）转字符串数组（适配前端展示）
     * - condition（数据库字符串）转ProductConditionEnum
     * - createTime格式化显示
     */
    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "jsonStrToArray") // JSON字符串转数组
    @Mapping(target = "condition", source = "condition", qualifiedByName = "strToConditionEnum") // 字符串转成色枚举
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "formatDateTime") // 时间格式化
    ProductDetailDTO entityToDetailDto(Product productEntity);


    /**
     * ProductUpdateDTO → ProductEntity（更新场景：请求参数转实体）
     * 适配文档：仅更新可修改字段，忽略sellerId、viewCount等不可改字段
     */
    @Mapping(target = "sellerId", ignore = true) // 卖家ID不可改
    @Mapping(target = "viewCount", ignore = true) // 浏览量自动增长
    @Mapping(target = "createTime", ignore = true) // 发布时间不可改
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now())") // 刷新更新时间
    @Mapping(target = "condition", source = "condition", qualifiedByName = "conditionEnumToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    Product updateDtoToEntity(ProductUpdateDTO updateDTO);


    // ------------------------------ 自定义转换方法（Java原生实现，无第三方依赖） ------------------------------
    /**
     * 校验JSON格式（确保imageUrls符合"[\"url1\",\"url2\"]"格式，避免非法数据存入数据库）
     */
    @Named("validateJsonFormat")
    default String validateJsonFormat(String jsonStr) {
        if (jsonStr == null || !jsonStr.startsWith("[") || !jsonStr.endsWith("]")) {
            throw new IllegalArgumentException("图片URL列表格式错误，需为JSON数组格式（如[\"url1\",\"url2\"]）");
        }
        return jsonStr;
    }

    /**
     * JSON字符串 → 字符串数组（原生正则提取，适配ProductDetailDTO的imageUrls字段）
     * 示例："[\"https://img1.jpg\",\"https://img2.jpg\"]" → ["https://img1.jpg", "https://img2.jpg"]
     */
    @Named("jsonStrToArray")
    default String[] jsonStrToArray(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return new String[0];
        }
        Matcher matcher = URL_EXTRACT_PATTERN.matcher(jsonStr);
        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            urls.add(matcher.group(1)); // 提取匹配的URL（去除双引号）
        }
        return urls.toArray(new String[0]);
    }

    /**
     * ProductConditionEnum → 字符串（适配product表condition字段存储）
     */
    @Named("conditionEnumToString")
    default String conditionEnumToString(ProductConditionEnum conditionEnum) {
        return conditionEnum == null ? null : conditionEnum.name();
    }

    /**
     * 字符串 → ProductConditionEnum（适配ProductDetailDTO的condition枚举展示）
     */
    @Named("strToConditionEnum")
    default ProductConditionEnum strToConditionEnum(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return ProductConditionEnum.valueOf(str);
    }

    /**
     * ProductStatusEnum → 字符串（适配product表status字段存储）
     */
    @Named("statusEnumToString")
    default String statusEnumToString(ProductStatusEnum statusEnum) {
        return statusEnum == null ? null : statusEnum.name();
    }

    /**
     * LocalDateTime → 格式化字符串（yyyy-MM-dd HH:mm:ss，适配前端展示）
     */
    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_FORMATTER);
    }
}
