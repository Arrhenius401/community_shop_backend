package xyz.graygoo401.ai.dao.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import xyz.graygoo401.ai.dao.entity.ChatMessage;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.graygoo401.ai.convert.SpringAiMessageConvert.chatMessageToMessage;
import static xyz.graygoo401.ai.convert.SpringAiMessageConvert.messageToChatMessage;

/**
 * 使用 Redis 实现的 Spring AI 会话记忆仓库方言
 */
@Slf4j
@Component
public class RedisChatMemoryRepositoryDialect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 活跃会话 ID 的 SET 所使用的 KEY
    private static final String CACHE_CONVERSATION_KEY = "chat:conversation_ids";
    // 消息列表的 KEY 前缀
    private static final String CACHE_MESSAGE_LIST_PREFIX = "chat:messages:";

    // 缓存TTL
    private static final Duration CACHE_TTL_CONVERSATION = Duration.ofMinutes(30);
    private static final Duration CACHE_TTL_MESSAGE = Duration.ofMinutes(30);

    private static final int MAX_HISTORY_SIZE = 100;
    private static final int MESSAGE_FETCH_LIMIT = 21;

    /**
     * 获取所有活跃会话ID
     * Redis数据结构：Set（无序且唯一）
     * 用于快速获取当前所有存在的会话ID
     */
    public List<String> findConversationIds() {
        // 1. 获取所有活跃会话ID
        Set<Object> members = redisTemplate.opsForSet().members(CACHE_CONVERSATION_KEY);

        // 2.刷新集合TTL
        redisTemplate.expire(CACHE_CONVERSATION_KEY, CACHE_TTL_CONVERSATION);

        // 3. 转换为字符串列表并返回
        return Optional.ofNullable(members)
                .filter(m -> !m.isEmpty())
                .map(m -> m.stream().map(Object::toString).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * 根据会话ID获取该会话的所有消息列表（多轮对话历史）反序列化
     * Redis数据结构：List（有序）
     * 按消息顺序返回，方便构造对话上下文
     */
    public List<Message> findByConversationId(String conversationId) {
        // 1. 拼接消息列表的key
        String key = getMessageKey(conversationId);
        Long size = redisTemplate.opsForList().size(key);
        if(size == null || size == 0L){
            return Collections.emptyList();
        }

        // 2. 获取该会话的所有消息列表
        long start = Math.max(0, size - MESSAGE_FETCH_LIMIT);
        List<Object> messageObjects = redisTemplate.opsForList().range(key, start, -1);
        if(CollectionUtils.isEmpty(messageObjects)){
            return Collections.emptyList();
        }

        // 3. 刷新消息列表TTL
        redisTemplate.expire(key, CACHE_TTL_MESSAGE);

        // 4. 反序列化消息列表
        return messageObjects.stream()
                .map(this::objectToMessage)
                .filter(Objects::nonNull) // 过滤转换失败的消息
                .collect(Collectors.toList());
    }

    /**
     * 保存一批消息到指定会话中，追加到消息列表末尾
     * Redis数据结构：List（右侧追加）
     * 并且保证会话ID存在于会话ID集合中
     */
    public void saveAll(String conversationId, List<Message> messages) {
        // 1. 忽略空消息
        if(CollectionUtils.isEmpty(messages)) return;

        // 2. 删除旧消息
        String key = getMessageKey(conversationId);
        redisTemplate.delete(key);

        // 3. 刷新会话id集合
        redisTemplate.opsForSet().add(CACHE_CONVERSATION_KEY, conversationId);
        redisTemplate.expire(CACHE_CONVERSATION_KEY, CACHE_TTL_CONVERSATION);

        // 4. 处理消息（过滤无效消息并添加时间戳）
        List<ChatMessage> validMessages = messages.stream()
                .filter(Objects::nonNull)
                .filter(m -> m.getText() != null && m.getMessageType() != null)
                .map(message -> messageToChatMessage(conversationId, message)) // 提取为独立方法
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validMessages)) {
            log.debug("没有有效的消息可保存，会话ID: {}", conversationId);
            return;
        }

        // 5. 保存消息并限制列表大小
        redisTemplate.opsForList().rightPushAll(key, validMessages.toArray());
        redisTemplate.expire(key, CACHE_TTL_MESSAGE);
        redisTemplate.opsForList().trim(key, -MAX_HISTORY_SIZE, -1);
    }

    /**
     * 删除指定会话的所有消息以及会话ID集合中的对应ID
     * Redis数据结构：删除List + Set中元素
     */
    public void deleteByConversationId(String conversationId) {

        // 1. 删除指定会话的所有消息
        String key = getMessageKey(conversationId);
        redisTemplate.delete(key);

        // 2. 删除会话ID集合中的对应ID
        redisTemplate.opsForSet().remove(CACHE_CONVERSATION_KEY, conversationId);
    }

    /**
     * 生成消息列表的Redis键
     */
    private String getMessageKey(String conversationId) {
        return CACHE_MESSAGE_LIST_PREFIX + conversationId;
    }

    private Message objectToMessage(Object object) {
        try {
            ChatMessage chatMessage;

            // 处理两种情况：直接的DTO对象或需要从JSON转换的情况
            if (object instanceof ChatMessage) {
                chatMessage = (ChatMessage) object;
            } else {
                String json = objectMapper.writeValueAsString(object);
                chatMessage = objectMapper.readValue(json, ChatMessage.class);
            }

            return chatMessageToMessage(chatMessage);
        } catch (Exception e) {
            log.error("从DTO转换为Message失败，对象: {}", object, e);
            return null;
        }
    }
}