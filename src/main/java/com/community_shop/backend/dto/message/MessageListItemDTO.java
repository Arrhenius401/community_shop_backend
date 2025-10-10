package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息列表项DTO（配合PageResult使用，用于消息中心列表页）
 */
@Data
public class MessageListItemDTO {

    /** 消息ID */
    private Long messageId;

    /** 内容摘要（前30字，适配列表展示） */
    private String contentSummary;

    /** 发送者信息（脱敏） */
    private Sender sender;

    /** 消息类型（用于展示不同图标） */
    private MessageTypeEnum type;

    /** 消息状态（用于显示未读红点） */
    private MessageStatusEnum status;

    /** 发送时间 */
    private LocalDateTime createTime;

    /** 是否有附件（用于显示附件图标） */
    private Boolean hasAttachment;

    /**
     * 发送者信息内部类（脱敏）
     */
    @Data
    public static class Sender {
        private Long userId;       // 发送者ID（系统消息为固定值）
        private String username;   // 发送者昵称（系统消息显示“系统通知”）
        private String avatarUrl;  // 发送者头像（系统消息用系统图标）
    }
}
