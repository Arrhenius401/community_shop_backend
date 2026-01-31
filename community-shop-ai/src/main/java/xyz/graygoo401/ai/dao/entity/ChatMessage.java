package xyz.graygoo401.ai.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.ai.enums.ChatMessageTypeEnum;

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
    @TableField(value = "chat_session_id")
    private String chatSessionId;

    /** 消息角色 */
    @TableField(value = "message_type")
    private ChatMessageTypeEnum messageType;

    /** 消息内容 */
    @TableField(value = "content")
    private String content;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP，无需手动设置） */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（用于同步Redis和MySQL,数据库默认 ON UPDATE CURRENT_TIMESTAMP，无需手动设置） */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
