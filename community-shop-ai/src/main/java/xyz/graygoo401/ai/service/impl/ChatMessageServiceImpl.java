package xyz.graygoo401.ai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.graygoo401.ai.convert.ChatMessageConvert;
import xyz.graygoo401.ai.dao.entity.ChatMessage;
import xyz.graygoo401.ai.dao.mapper.ChatMessageMapper;
import xyz.graygoo401.ai.service.base.ChatMessageService;
import xyz.graygoo401.api.ai.dto.message.ChatMessageDetailDTO;
import xyz.graygoo401.common.service.BaseServiceImpl;

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