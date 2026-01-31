package xyz.graygoo401.ai.convert;

import org.mapstruct.Mapper;
import xyz.graygoo401.ai.dao.entity.ChatSession;
import xyz.graygoo401.api.ai.dto.session.ChatSessionDetailDTO;
import xyz.graygoo401.api.ai.dto.session.ChatSessionListItem;

/**
 * ChatSession 模块对象转换器
 * 处理 ChatSession 实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring")  // 声明为 Spring 组件，支持依赖注入
public interface ChatSessionConvert {

    /**
     * ChatSession 实体 -> ChatSessionDetailDTO（会话消息详情响应）
     * 映射说明：直接匹配同名字段，枚举类型因类型一致可自动映射
     */
    ChatSessionDetailDTO chatSessionToChatSessionDetailDTO(ChatSession chatSession);

    /**
     * ChatSession 实体 -> ChatSessionListItem（会话列表项）
     * 映射说明：直接匹配同名字段，枚举类型因类型一致可自动映射
     */
    ChatSessionListItem chatSessionToChatSessionListItem(ChatSession chatSession);
}