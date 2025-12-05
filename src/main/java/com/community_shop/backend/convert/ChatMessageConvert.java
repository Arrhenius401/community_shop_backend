package com.community_shop.backend.convert;

import com.community_shop.backend.dto.chat.ChatMessageDetailDTO;
import com.community_shop.backend.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * ChatMessage 模块对象转换器
 * 处理 ChatMessage 实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring")  // 声明为 Spring 组件，支持依赖注入
public interface ChatMessageConvert {

    // 单例实例（非 Spring 环境使用）
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * ChatMessage 实体 -> ChatMessageDetailDTO（会话消息详情响应）
     * 映射说明：直接匹配同名字段，枚举类型因类型一致可自动映射
     */
    ChatMessageDetailDTO chatMessageToChatMessageDTO(ChatMessage chatMessage);

    /**
     * 批量转换 ChatMessage 列表 -> ChatMessageDTO 列表
     */
    List<ChatMessageDetailDTO> chatMessageListToChatMessageDetailDTOList(List<ChatMessage> chatMessage);
}