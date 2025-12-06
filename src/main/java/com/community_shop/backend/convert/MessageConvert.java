package com.community_shop.backend.convert;

import com.community_shop.backend.dto.message.*;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.enums.code.MessageStatusEnum;
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
public interface MessageConvert {

    // 单例实例（非 Spring 环境使用）
    MessageConvert INSTANCE = Mappers.getMapper(MessageConvert.class);

    /**
     * Message 实体 -> MessageDetailDTO（消息详情响应）
     * 映射说明：
     * 1. 实体 msgId 对应 DTO messageId
     * 2. 实体 isRead/isDeleted 组合转换为 DTO status 枚举
     * 3. 实体 orderId 对应 DTO businessId
     * 4. 实体 JSON 格式附件转为 DTO List<String>
     */
    @Mappings({
            @Mapping(target = "messageId", source = "msgId"),
            @Mapping(target = "sender.userId", source = "senderId"),
            @Mapping(target = "sender.username", ignore = true), // 需关联 User 实体脱敏后赋值
    })
    MessageDetailDTO messageToMessageDetailDTO(Message message);

    /**
     * Message 实体 -> MessageListItemDTO（消息列表项）
     * 映射说明：
     * 1. 生成内容摘要（前30字）
     * 2. 判断是否有附件（非空即视为有）
     */
    @Mappings({
            @Mapping(target = "messageId", source = "msgId"),
            @Mapping(target = "contentSummary", expression = "java(getContentSummary(message.getContent()))"),
    })
    MessageListItemDTO messageToMessageListItemDTO(Message message);

    /**
     * MessageSendDTO（消息发送请求）-> Message 实体
     * 映射说明：
     * 1. 初始化未读、未删除状态
     * 2. 附件列表以 JSON 字符串存储
     * 3. 系统消息 senderId 默认为 0，receiverId 默认为 -1
     */
    @Mappings({
            @Mapping(target = "msgId", ignore = true),
            @Mapping(target = "title", defaultValue = ""), // 私信无标题，默认空
            @Mapping(target = "orderId", source = "businessId"),
            @Mapping(target = "isRead", constant = "false"), // 初始未读
            @Mapping(target = "isDeleted", constant = "false"), // 初始未删除
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            // 系统消息特殊处理：senderId=0，receiverId=-1
            @Mapping(target = "receiverId", expression = "java(dto.getType() == com.community_shop.backend.enums.CodeEnum.MessageTypeEnum.SYSTEM ? (long)-1 : dto.getReceiverId())")
    })
    Message messageSendDtoToMessage(MessageSendDTO dto);

    /**
     * MessageStatusUpdateDTO（消息状态更新请求）-> Message 实体
     * 映射说明：根据目标状态更新 isRead/isDeleted 字段
     */
    @Mappings({
            @Mapping(target = "msgId", source = "messageId"),
            @Mapping(target = "isRead", expression = "java(dto.getTargetStatus() == com.community_shop.backend.enums.CodeEnum.MessageStatusEnum.READ)"),
            @Mapping(target = "isDeleted", expression = "java(dto.getTargetStatus() == com.community_shop.backend.enums.CodeEnum.MessageStatusEnum.DELETED)"),
            @Mapping(target = "updateTime", ignore = true), // 由业务逻辑更新时间
    })
    Message messageStatusUpdateDtoToMessage(MessageStatusUpdateDTO dto);

    /**
     * 批量转换 Message 列表 -> MessageListItemDTO 列表
     */
    List<MessageListItemDTO> messageListToMessageListItemList(List<Message> messages);

    /**
     * 批量转换 Message 列表 -> MessageDetailDTO 列表
     */
    List<MessageDetailDTO> messageListToMessageDetailList(List<Message> messages);


    // ------------------------------ 辅助方法 ------------------------------
    /**
     * 根据 isRead 和 isDeleted 转换为 MessageStatusEnum
     */
    default MessageStatusEnum getMessageStatus(Boolean isRead, Boolean isDeleted) {
        if (Boolean.TRUE.equals(isDeleted)) {
            return MessageStatusEnum.DELETED;
        }
        return Boolean.TRUE.equals(isRead) ? MessageStatusEnum.READ : MessageStatusEnum.UNREAD;
    }

    /**
     * 生成消息内容摘要（前30字，超出用"..."省略）
     */
    default String getContentSummary(String content) {
        if (content == null || content.length() <= 30) {
            return content;
        }
        return content.substring(0, 30) + "...";
    }

    /**
     * 判断是否有附件（附件 JSON 字符串非空即视为有）
     */
    default Boolean hasAttachment(String attachmentsJson) {
        return attachmentsJson != null && !attachmentsJson.isEmpty() && !"[]".equals(attachmentsJson);
    }

    /**
     * JSON 字符串转 List<String>（附件列表）
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
     * List<String> 转 JSON 字符串（附件列表存储）
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
