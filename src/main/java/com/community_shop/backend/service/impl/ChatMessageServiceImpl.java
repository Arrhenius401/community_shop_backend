package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.ChatMessageConvert;
import com.community_shop.backend.dao.mapper.ChatMessageMapper;
import com.community_shop.backend.dto.chat.ChatMessageDetailDTO;
import com.community_shop.backend.entity.ChatMessage;
import com.community_shop.backend.enums.code.ChatMessageTypeEnum;
import com.community_shop.backend.exception.error.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.service.base.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI会话消息模块 Service 实现类
 */
@Service
public class ChatMessageServiceImpl extends BaseServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatMessageConvert chatMessageConvert;

    /**
     * 根据会话ID获取会话记录
     * @param sessionId 会话ID
     * @return 会话记录列表
     */
    @Override
    public List<ChatMessageDetailDTO> getBySessionId(String sessionId) {
        return chatMessageConvert.chatMessageListToChatMessageDetailDTOList(chatMessageMapper.selectBySessionId(sessionId));
    }

    /**
     * 判断是否是第一条消息
     * @param sessionId 会话ID
     * @return 是否是第一条消息
     */
    public Boolean isFirstMessage(String sessionId) {
        return chatMessageMapper.countBySessionId(sessionId) == 0;
    }

}