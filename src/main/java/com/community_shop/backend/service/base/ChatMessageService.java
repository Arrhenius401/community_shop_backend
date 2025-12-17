package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.chat.ChatMessageDetailDTO;
import com.community_shop.backend.entity.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 会话消息模块 Service 接口
 */
@Service
public interface ChatMessageService extends BaseService<ChatMessage> {

    /**
     * 根据会话ID获取会话记录
     * @param sessionId 会话ID
     * @return 会话记录列表
     */
    List<ChatMessageDetailDTO> getBySessionId(String sessionId);

    /**
     * 判断是否是第一条消息
     * @param sessionId 会话ID
     * @return 是否是第一条消息
     */
    Boolean isFirstMessage(String sessionId);

}
