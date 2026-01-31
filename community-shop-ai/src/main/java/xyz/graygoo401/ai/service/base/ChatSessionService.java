package xyz.graygoo401.ai.service.base;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xyz.graygoo401.ai.dao.entity.ChatSession;
import xyz.graygoo401.api.ai.dto.session.ChatPromptDTO;
import xyz.graygoo401.api.ai.dto.session.ChatSessionDetailDTO;
import xyz.graygoo401.api.ai.dto.session.ChatSessionListItem;
import xyz.graygoo401.api.ai.dto.session.ChatSessionQueryDTO;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.service.BaseService;

/**
 * AI 会话模块 Service 接口（消息->会话）
 */
@Service
public interface ChatSessionService extends BaseService<ChatSession> {

    /**
     * AI 聊天
     * @param prompt 聊天参数
     * @return 聊天结果
     */
    Flux<String> chatWithAi(ChatPromptDTO prompt);

    /**
     * 创建新的会话ID
     * @param userId 用户ID
     * @return 会话记录列表
     */
    String createSession(Long userId);

    /**
     * 生成会话标题
     * @param sessionId 会话ID
     * @param firstPrompt 第一个问题
     * @param firstAnswer 第一个答案
     * @return 生成的标题
     */
    String generateTitle(String sessionId, String firstPrompt, String firstAnswer);

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