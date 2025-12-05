package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.enums.CodeEnum.ChatMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 会话消息实体类
 * 存储会话中的每条消息
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@TableName("chat_message")
public class ChatMessage {

    /** 主键ID */
    @TableId(value = "chat_message_id")
    private String chatMessageId;

    /** 会话ID */
    private String chatSessionId;

    /** 消息角色 */
    private ChatMessageTypeEnum messageType;

    /** 消息内容 */
    private String content;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP，无需手动设置） */
    private LocalDateTime createTime;

    /** 更新时间（用于同步Redis和MySQL,数据库默认 ON UPDATE CURRENT_TIMESTAMP，无需手动设置） */
    private LocalDateTime updateTime;
}
