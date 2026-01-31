package xyz.graygoo401.ai.convert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import xyz.graygoo401.ai.dao.entity.ChatMessage;
import xyz.graygoo401.api.ai.enums.ChatMessageTypeEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static xyz.graygoo401.ai.constants.AiChatConstants.CHAT_MESSAGE_ID_PREFIX;

/**
 * 消息转换工具类：负责 ChatMessage 与 Spring AI Message 的相互转换
 */
@Slf4j
public class SpringAiMessageConvert {

    // 默认的日期时间格式（与LocalDateTime.toString()一致）
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 元数据中的键
    private static final String METADATA_KEY_CREATE_TIME = "create_time";
    private static final String METADATA_KEY_UPDATE_TIME = "update_time";
    private static final String METADATA_KEY_CHAT_MESSAGE_ID = "chat_message_id";

    /**
     * 将metadata中的数据填充到ChatMessage中
     * @param message 本地项目自定义消息
     * @param metadata 消息元数据
     * @return 填充后的ChatMessage
     */
    public static ChatMessage parseMetadata(ChatMessage message, Map<String, Object> metadata) {
        // 1. metadata 为空时，无需解析，直接返回原消息
        if (metadata == null) {
            return message;
        }

        try {
            // 2. 解析 create_time 字段
            // 模式匹配 instanceof（Pattern Matching for instanceof）语法，如果不是，则返回 null；如果是，则将其转换为 String 类型并赋值给变量 createTimeStr，供后续代码使用
            if (metadata.containsKey(METADATA_KEY_CREATE_TIME) && (metadata.get(METADATA_KEY_CREATE_TIME) instanceof String createTimeStr)) {
                // 解析字符串为LocalDateTime
                message.setCreateTime(LocalDateTime.parse(createTimeStr, DEFAULT_FORMATTER));
            }

            // 3. 解析 update_time 字段
            if (metadata.containsKey(METADATA_KEY_UPDATE_TIME) && (metadata.get(METADATA_KEY_UPDATE_TIME) instanceof String updateTimeStr)) {
                message.setUpdateTime(LocalDateTime.parse(updateTimeStr, DEFAULT_FORMATTER));
            }

            // 4. 解析 chat_message_id 字段
            if (metadata.containsKey(METADATA_KEY_CHAT_MESSAGE_ID) && (metadata.get(METADATA_KEY_CHAT_MESSAGE_ID) instanceof String chatMessageId)) {
                message.setChatMessageId(chatMessageId);
            }

            return message;
        } catch (DateTimeParseException e) {
            log.error("解析 LocalDateTime 类型数据失败: ", e);
            return message;
        }
    }

    /**
     * ChatMessage 转 Spring AI Message
     * @param message 本地项目自定义消息
     * @return Spring AI Message
     */
    public static Message chatMessageToMessage(ChatMessage message) {
        // 1. 创建元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_KEY_CREATE_TIME, message.getCreateTime().toString());
        metadata.put(METADATA_KEY_UPDATE_TIME, message.getUpdateTime().toString());
        metadata.put(METADATA_KEY_CHAT_MESSAGE_ID, message.getChatMessageId());

        // 2. 根据消息类型创建消息
        return switch (message.getMessageType()) {
            case USER-> UserMessage.builder()
                    .text(message.getContent())
                    .metadata(metadata)
                    .build();
            case ASSISTANT -> new AssistantMessage(message.getContent(), metadata);
            case SYSTEM -> SystemMessage.builder()
                    .text(message.getContent())
                    .metadata(metadata)
                    .build();
            case TOOL -> new ToolResponseMessage(List.of(), metadata);
            default -> throw new IllegalArgumentException("未知消息类型: " + message.getMessageType());
        };
    }

    /**
     * Spring AI Message 转 ChatMessage
     * @param conversationId 会话ID
     * @param message Spring AI Message
     * @return ChatMessage 本地项目自定义消息
     */
    public static ChatMessage messageToChatMessage(String conversationId, Message message) {
        // 1. 创建 ChatMessage，转换基础数据
        ChatMessage chatMessage = ChatMessage.builder()
                .chatSessionId(conversationId)
                .content(message.getText())
                .messageType(ChatMessageTypeEnum.fromValue(message.getMessageType().getValue()))
                .build();

        // 2. 填充元数据
        return parseMetadata(chatMessage, message.getMetadata());
    }

    public static List<ChatMessage> fillBlankChatMessage(List<ChatMessage> chatMessageList) {
        // 1. 确保消息列表不为空
        if (chatMessageList == null || chatMessageList.isEmpty()) {
            return chatMessageList;
        }

        // 2. 筛选出新生成的 Message 实体列表后，填充缺失的字段数据
        List<ChatMessage> newMessageList = chatMessageList.stream()
                .filter(msg -> msg.getChatMessageId() == null)
                .map(msg -> {
                    msg.setChatMessageId(createMessageId());
                    msg.setCreateTime(LocalDateTime.now());
                    msg.setUpdateTime(LocalDateTime.now());
                    return msg;
                })
                .toList();
        return newMessageList;
    }

    private static String createMessageId() {
        return CHAT_MESSAGE_ID_PREFIX + UUID.randomUUID().toString();
    }
}
