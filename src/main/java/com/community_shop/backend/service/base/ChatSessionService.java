package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.chat.ChatSessionDetailDTO;
import com.community_shop.backend.dto.chat.ChatSessionListItem;
import com.community_shop.backend.dto.chat.ChatSessionQueryDTO;
import com.community_shop.backend.entity.ChatSession;
import org.springframework.stereotype.Service;

/**
 * AI 会话模块 Service 接口（消息->会话）
 */
@Service
public interface ChatSessionService extends BaseService<ChatSession>{

    /**
     * 创建新的会话ID
     * @param userId 用户ID
     * @return 会话记录列表
     */
    String createSessionId(Long userId);

    /**
     * 更新会话标题
     * @param sessionId 会话ID
     * @param title 标题
     * @return 是否成功
     */
    Boolean updateSessionTitle(String sessionId, String title);

    /**
     * 获取会话内容
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话内容
     */
    ChatSessionDetailDTO selectSessionById(String sessionId, Long userId);

    /**
     * 查询会话列表数量
     * @param queryDTO 查询参数
     * @return 会话列表
     */
    int countSessions(ChatSessionQueryDTO queryDTO);

    /**
     * 查询会话列表
     * @param queryDTO 查询参数
     * @return 会话列表
     */
    PageResult<ChatSessionListItem> querySessions(ChatSessionQueryDTO queryDTO);
}