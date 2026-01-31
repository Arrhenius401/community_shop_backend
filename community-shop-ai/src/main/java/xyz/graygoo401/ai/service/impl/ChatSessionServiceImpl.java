package xyz.graygoo401.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import xyz.graygoo401.ai.constants.AiPromptTemplateConstants;
import xyz.graygoo401.ai.convert.ChatSessionConvert;
import xyz.graygoo401.ai.dao.entity.ChatSession;
import xyz.graygoo401.ai.dao.mapper.ChatSessionMapper;
import xyz.graygoo401.ai.exception.error.AiErrorCode;
import xyz.graygoo401.ai.service.base.ChatMessageService;
import xyz.graygoo401.ai.service.base.ChatSessionService;
import xyz.graygoo401.api.ai.dto.message.ChatMessageDetailDTO;
import xyz.graygoo401.api.ai.dto.session.ChatPromptDTO;
import xyz.graygoo401.api.ai.dto.session.ChatSessionDetailDTO;
import xyz.graygoo401.api.ai.dto.session.ChatSessionListItem;
import xyz.graygoo401.api.ai.dto.session.ChatSessionQueryDTO;
import xyz.graygoo401.api.ai.enums.ChatSessionStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.api.user.feign.UserClient;
import xyz.graygoo401.common.exception.error.SystemErrorCode;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.service.BaseServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static xyz.graygoo401.ai.constants.AiChatConstants.CHAT_SESSION_ID_PREFIX;

/**
 * AI会话模块 Service 实现类
 */
@Service
public class ChatSessionServiceImpl extends BaseServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatSessionConvert chatSessionConvert;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * AI 聊天
     * @param prompt 聊天参数
     * @return 聊天结果流
     */
    @Override
    public Flux<String> chatWithAi(ChatPromptDTO prompt) {
        String sessionId = prompt.getChatSessionId();
        String firstPrompt = prompt.getPrompt();

        // 1. 检查是否是首次消息
        boolean isFirstMessage = chatMessageService.isFirstMessage(sessionId);

        // 2. 生成AI响应流
        Flux<String> responseFlux = chatClient.prompt()
                .user(firstPrompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content();

        // 3. 若为首次消息，收集AI回答后生成标题
        if (isFirstMessage) {
            // 缓存AI的完整回答（流式响应需要拼接）
            AtomicReference<StringBuilder> aiReplyBuilder = new AtomicReference<>(new StringBuilder());

            return responseFlux
                    // 收集每一段流式响应，拼接成完整回答
                    .doOnNext(segment -> aiReplyBuilder.get().append(segment))
                    // 响应完成后，生成标题
                    .doOnComplete(() -> {
                        String firstAnswer = aiReplyBuilder.get().toString();
                        // 异步生成标题（不阻塞前端）
                        CompletableFuture.runAsync(() ->
                                generateTitle(sessionId, firstPrompt, firstAnswer)
                        );
                    });
        }

        // 4. 返回AI响应流
        return responseFlux;
    }

    /**
     * 创建会话ID（暂时不插入）
     * @param userId 用户ID
     * @return 会话ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String createSession(Long userId) {
        // 1. 校验用户
        if(userClient.getUserById(userId) == null){
            throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
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
        chatSession.setTitle("新对话");

        // 4. 插入会话记录
        int updateRows = chatSessionMapper.insert(chatSession);
        if(updateRows <= 0) {
            throw new BusinessException(SystemErrorCode.DATA_INSERT_FAILED);
        }

        return sessionId;
    }

    /**
     * 生成会话标题
     * @param sessionId 会话ID
     * @param firstPrompt 第一个问题
     * @param firstAnswer 第一个答案
     * @return 生成的标题
     */
    @Override
    public String generateTitle(String sessionId, String firstPrompt, String firstAnswer) {
        // 1. 校验会话是否存在
        ChatSession chatSession = chatSessionMapper.selectById(sessionId);
        if(chatSession == null) {
            throw new BusinessException(AiErrorCode.AI_CHAT_SESSION_NOT_EXISTS);
        }

        // 2. 调用 AI 生成标题
        String titlePrompt = String.format(AiPromptTemplateConstants.GENERATE_TITLE, firstPrompt, firstAnswer);
        String title = chatClient.prompt()
                .system(AiPromptTemplateConstants.GENERATE_TITLE_ROLE)
                .user(titlePrompt)
                .call()
                .content();

        int updateRows = chatSessionMapper.updateTitle(sessionId, title);
        if(updateRows <= 0) {
            throw new BusinessException(SystemErrorCode.DATA_UPDATE_FAILED);
        }

        return title;
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
        UserDTO user = userClient.getUserById(userId).getData();
        if (user != null && !user.isAdmin() && !userId.equals(chatSession.getUserId())) {
            throw new BusinessException(SystemErrorCode.PERMISSION_DENIED);
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

    // ---------------------- 私有辅助方法 ----------------------
    private boolean updateSessionTitle(String sessionId, String title) {
        int updateRows = chatSessionMapper.updateTitle(sessionId, title);
        if(updateRows <= 0) {
            throw new BusinessException(SystemErrorCode.DATA_UPDATE_FAILED);
        }

        return updateRows > 0;
    }

}