package com.community_shop.backend.config;

import com.community_shop.backend.dao.repository.DoubleLayerChatMemoryRepository;
import com.community_shop.backend.dao.repository.RedisChatMemoryRepository;
import com.community_shop.backend.dao.repository.RedisChatMemoryRepositoryDialect;
import com.community_shop.backend.utils.tool.DateTimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.community_shop.backend.utils.constants.AiPromptTemplateConstants.*;

/**
 * AI 客户端配置
 */
@Configuration
public class AiConfig {

    /**
     * 聊天记忆存储库（使用Redis）
     * @param dialect 使用的聊天存储库方言
     * @return 聊天存储库
     */
    @Bean("redisChatMemoryRepository")
    public ChatMemoryRepository redisChatMemoryRepository(RedisChatMemoryRepositoryDialect dialect) {
        return new RedisChatMemoryRepository(dialect);
    }

    /**
     * 聊天记忆存储库（使用MySQL+Redis双层架构）
     * @return 聊天存储库
     */
    @Bean("doubleLayerChatMemoryRepository")
    @Primary
    public ChatMemoryRepository doubleLayerChatMemoryRepository() {
        return new DoubleLayerChatMemoryRepository();
    }

    /**
     * 聊天内存
     * @param chatMemoryRepository 聊天内存存储方式
     * @return 聊天内存
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    /**
     * 聊天模型
     * @return 聊天模型
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory,
                                     DateTimeTool dateTimeTool
    ) {


        return ChatClient
                // 注入底层 Model
                .builder(model)
                // 默认系统提示词
                .defaultSystem(SYSTEM_ROLE)
                .defaultSystem(TOOL_CALLING_RULES)
                .defaultSystem(SAFETY_BOUNDARIES)
                .defaultSystem(INTERACTION_RULES)
                // 默认顾问
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // 默认注册工具
                .defaultTools(dateTimeTool)
                .build();

    }
}
