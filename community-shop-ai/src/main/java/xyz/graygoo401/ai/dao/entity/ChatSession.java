package xyz.graygoo401.ai.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.ai.enums.ChatSessionStatusEnum;

import java.time.LocalDateTime;

/**
 * AI 会话实体类
 * 存储会话的基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_session")
public class ChatSession {

    /** 会话ID */
    @TableId(value = "chat_session_id")
    private String chatSessionId;

    /** 用户ID */
    @TableField(value = "user_id")
    private Long userId;

    /** 会话摘要（根据用户的第一个消息给出） */
    @TableField(value = "title")
    private String title;

    /** 创建时间*/
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 会话状态 */
    @TableField(value = "status")
    private ChatSessionStatusEnum status;
}