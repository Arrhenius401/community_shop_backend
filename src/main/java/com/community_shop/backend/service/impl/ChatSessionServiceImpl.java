package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.ChatSessionConvert;
import com.community_shop.backend.dao.mapper.ChatSessionMapper;
import com.community_shop.backend.dao.mapper.UserMapper;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.chat.ChatMessageDetailDTO;
import com.community_shop.backend.dto.chat.ChatSessionDetailDTO;
import com.community_shop.backend.dto.chat.ChatSessionListItem;
import com.community_shop.backend.dto.chat.ChatSessionQueryDTO;
import com.community_shop.backend.entity.ChatSession;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.error.ErrorCode;
import com.community_shop.backend.enums.simple.ChatSessionStatusEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.service.base.ChatMessageService;
import com.community_shop.backend.service.base.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.community_shop.backend.utils.constants.AiChatConstants.CHAT_SESSION_ID_PREFIX;

/**
 * AI会话模块 Service 实现类
 */
@Service
public class ChatSessionServiceImpl extends BaseServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatSessionConvert chatSessionConvert;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 创建会话ID（暂时不插入）
     * @param userId 用户ID
     * @return 会话ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String createSessionId(Long userId) {
        // 1. 校验用户
        if(userMapper.selectById(userId) == null){
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 2. 创建会话ID
        // 此处不需要锁，因为 UUID 在理论上是全局唯一的，其设计目标是确保在分布式系统中，无需这样协调就能生成不重复的标识符
        String sessionId = CHAT_SESSION_ID_PREFIX + UUID.randomUUID();

        // 3. 创建会话记录实体
        ChatSession chatSession = new ChatSession();
        chatSession.setChatSessionId(sessionId);
        chatSession.setUserId(userId);
        chatSession.setStatus(ChatSessionStatusEnum.STARTED);
        chatSession.setCreateTime(LocalDateTime.now());
        chatSession.setUpdateTime(LocalDateTime.now());
        chatSession.setSummary(userId + " 的会话: " + sessionId);

        // 4. 插入会话记录
        int updateRows = chatSessionMapper.insert(chatSession);
        if(updateRows <= 0) {
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
        }

        return sessionId;
    }

    @Override
    public Boolean updateSessionSummary(String sessionId, String summary) {
        // 1. 创建会话记录实体
        ChatSession session = new ChatSession();
        session.setChatSessionId(sessionId);
        session.setSummary(summary); // 设置摘要（如用户第一条消息的摘要）
        session.setUpdateTime(LocalDateTime.now()); // 更新时间戳

        // 2. 根据ID更新非空字段（MyBatis-Plus的更新方法）
        int updateRows = chatSessionMapper.updateById(session);
        if(updateRows <= 0) {
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        return true;
    }

    /**
     * 查询会话详情
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话详情
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public ChatSessionDetailDTO selectSessionById(String sessionId, Long userId) {
        // 1. 查询会话
        ChatSession chatSession = chatSessionMapper.selectById(sessionId);
        if (chatSession == null) {
            return null;
        }

        // 2. 检验用户权限
        User user = userMapper.selectById(userId);
        if (!user.isAdmin() && !userId.equals(chatSession.getUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 3. 创建会话详情DTO
        ChatSessionDetailDTO chatSessionDetailDTO = chatSessionConvert.chatSessionToChatSessionDetailDTO(chatSession);

        // 4. 获取消息列表
        List<ChatMessageDetailDTO> messages = chatMessageService.getBySessionId(sessionId);
        chatSessionDetailDTO.setMessages(messages);

        return chatSessionDetailDTO;
    }

    /**
     * 查询会话列表数量
     * @param queryDTO 查询参数
     * @return 会话列表
     */
    @Override
    public int countSessions(ChatSessionQueryDTO queryDTO) {
        return chatSessionMapper.countByQuery(queryDTO);
    }

    /**
     * 查询会话列表
     * @param queryDTO 筛选参数
     * @return 会话列表
     */
    @Override
    public PageResult<ChatSessionListItem> querySessions(ChatSessionQueryDTO queryDTO) {
        // 1. 查询总数和列表（匹配UserMapper.countByAllParam和selectByAllParam）
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        queryDTO.setOffset(offset);

        long total = chatSessionMapper.countByQuery(queryDTO);
        List<ChatSession> list = chatSessionMapper.selectByQuery(queryDTO);

        // 2. 转换为ChatSessionDetailDTO列表
        List<ChatSessionListItem> dtoList = list.stream()
                .map(chatSessionConvert::chatSessionToChatSessionListItem)
                .toList();

        // 3. 封装PageResult
        long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return new PageResult<ChatSessionListItem>(total, totalPages, dtoList, pageNum, pageSize);
    }

}