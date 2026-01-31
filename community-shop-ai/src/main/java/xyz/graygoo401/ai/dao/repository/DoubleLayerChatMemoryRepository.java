package xyz.graygoo401.ai.dao.repository;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import xyz.graygoo401.ai.convert.SpringAiMessageConvert;
import xyz.graygoo401.ai.dao.entity.ChatMessage;
import xyz.graygoo401.ai.dao.mapper.ChatMessageMapper;

import java.util.List;

/**
 * MySQL + Redis 双层缓存实现的会话记忆仓库
 * 优先从Redis读取，未命中则从MySQL加载并同步到Redis；写入时同时更新两者
 */
@Repository
public class DoubleLayerChatMemoryRepository implements ChatMemoryRepository {

    @Autowired
    private RedisChatMemoryRepositoryDialect redisDialect;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 获取会话ID列表
     * 若Redis缓存为空则从MySQL加载并同步到Redis
     */
    @NonNull
    @Override
    public List<String> findConversationIds() {
        // 1. 从Redis获取会话ID
        List<String> redisIds = redisDialect.findConversationIds();
        if (!redisIds.isEmpty()) {
            return redisIds;
        }

        // 2. 若为空则从MySQL加载并同步到Redis
        List<String> mysqlIds = chatMessageMapper.selectDistinctSessionIds();

        // 3. 同步到Redis
        mysqlIds.forEach(id -> redisDialect.saveAll(id, findByConversationId(id)));
        return mysqlIds;
    }

    /**
     * 根据会话ID获取消息列表
     * 若Redis缓存为空则从MySQL加载并同步到Redis
     */
    @NonNull
    @Override
    public List<Message> findByConversationId(@NonNull String conversationId) {
        // 1. 先查Redis缓存
        List<Message> redisMessages = redisDialect.findByConversationId(conversationId);
        if (!redisMessages.isEmpty()) {
            return redisMessages;
        }

        // 2. Redis未命中，查MySQL
        List<ChatMessage> poList = chatMessageMapper.selectBySessionId(conversationId);
        if (poList.isEmpty()) {
            return List.of();
        }

        // 3. 转换为Message并同步到Redis
        List<Message> messages = poList.stream()
                .map(SpringAiMessageConvert::chatMessageToMessage)
                .toList();
        redisDialect.saveAll(conversationId, messages);
        return messages;
    }

    /**
     * 保存会话消息
     * 同时同步到MySQL和Redis
     */
    @Override
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
        // 1. 确保消息列表不为空
        if (messages.isEmpty()) {
            return;
        }

        // 2. 数据类型转换，将 Message 转换为 ChatMessage
        List<ChatMessage> chatMessageList = messages.stream()
                .map(msg -> SpringAiMessageConvert.messageToChatMessage(conversationId, msg))
                .toList();

        // 3. 过滤出新产生的message，将它们插入数据库
        List<ChatMessage> newMessageList = SpringAiMessageConvert.fillBlankChatMessage(chatMessageList);
        chatMessageMapper.batchInsert(newMessageList);

        // 2. 再保存至 Redis 数据库（缓存）
        redisDialect.saveAll(conversationId, messages);
    }

    /**
     * 删除会话消息
     * 同时同步到MySQL和Redis
     */
    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        // 1. 先删除MySQL数据
        chatMessageMapper.deleteBySessionId(conversationId);
        // 2. 再删除Redis缓存
        redisDialect.deleteByConversationId(conversationId);
    }

}