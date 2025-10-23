package com.community_shop.backend.convert;

import com.community_shop.backend.dto.evaluation.EvaluationCreateDTO;
import com.community_shop.backend.dto.evaluation.EvaluationDetailDTO;
import com.community_shop.backend.dto.evaluation.EvaluationListItemDTO;
import com.community_shop.backend.dto.evaluation.EvaluationUpdateDTO;
import com.community_shop.backend.entity.Evaluation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Message 模块对象转换器
 * 处理 Message 实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring", uses = ObjectMapper.class)
public interface EvaluationConvert {

    // 单例实例（非 Spring 环境使用）
    EvaluationConvert INSTANCE = Mappers.getMapper(EvaluationConvert.class);

    /**
     * Evaluation 实体 -> EvaluationDetailDTO（评价详情响应）
     * 映射说明：
     * 1. 实体 evalId 对应 DTO evalId
     * 2. 关联商品信息需 Service 层补充
     * 3. 评价人信息需脱敏后赋值
     */
    @Mappings({
            @Mapping(target = "evalId", source = "evalId"),
            @Mapping(target = "evaluator.userId", source = "userId"),
            @Mapping(target = "evaluator.username", ignore = true), // 需脱敏后赋值
            @Mapping(target = "evaluator.avatarUrl", ignore = true), // 需关联 User 实体查询
            @Mapping(target = "product.productId", ignore = true), // 需提前通过 orderId 查询商品 ID 存入实体
            @Mapping(target = "product.productName", ignore = true), // 需关联 Product 实体查询
            @Mapping(target = "product.productImage", ignore = true), // 需关联 Product 实体查询
    })
    EvaluationDetailDTO evaluationToEvaluationDetailDTO(Evaluation evaluation);

    /**
     * Evaluation 实体 -> EvaluationListItemDTO（评价列表项）
     * 映射说明：
     * 1. 生成内容摘要（前100字）
     * 2. 图片列表取前3张作为缩略图
     */
    @Mappings({
            @Mapping(target = "evalId", source = "evalId"),
            @Mapping(target = "contentSummary", expression = "java(getContentSummary(evaluation.getContent()))"),
    })
    EvaluationListItemDTO evaluationToEvaluationListItemDTO(Evaluation evaluation);

    /**
     * EvaluationCreateDTO（评价创建请求）-> Evaluation 实体
     * 映射说明：
     * 1. 初始化评价状态为正常，有用数为 0
     * 2. 图片列表以 JSON 字符串存储
     * 3. 卖家 ID 需通过 orderId 查询订单后补充
     */
    @Mappings({
            @Mapping(target = "evalId", ignore = true),
            @Mapping(target = "sellerId", ignore = true), // 需通过 orderId 查询订单获取卖家 ID
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum.NORMAL)"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true)
    })
    Evaluation evaluationCreateDtoToEvaluation(EvaluationCreateDTO dto);

    /**
     * 批量转换 Evaluation 列表 -> EvaluationListItemDTO 列表
     */
    List<EvaluationListItemDTO> evaluationListToEvaluationListItemList(List<Evaluation> evaluations);

    /**
     * 批量转换 Evaluation 列表 -> EvaluationDetailDTO 列表
     */
    List<EvaluationDetailDTO> evaluationListToEvaluationDetailList(List<Evaluation> evaluations);


    // ------------------------------ 辅助方法 ------------------------------
    /**
     * 生成评价内容摘要（前100字，超出用"..."省略）
     */
    default String getContentSummary(String content) {
        if (content == null || content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }

    /**
     * 获取前3张图片作为缩略图
     */
    default List<String> getFirstThreeImages(String imagesJson) {
        List<String> images = jsonToList(imagesJson);
        if (images.size() <= 3) {
            return images;
        }
        return images.subList(0, 3);
    }

    /**
     * JSON 字符串转 List<String>（图片列表）
     */
    default List<String> jsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * List<String> 转 JSON 字符串（图片列表存储）
     */
    default String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
