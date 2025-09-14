package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息预览信息，用于展示用户最近 3 条未读消息的精简信息
 */
@Data
public class MessagePreviewDTO {

    /** 消息ID */
    private Long messageId;

    /** 发送者名称 */
    private String senderName;

    /** 消息内容摘要 */
    private String contentSummary;

    /** 消息类型 */
    private MessageTypeEnum type;

    /** 发送时间 */
    private LocalDateTime createTime;

    /** 业务ID（如订单ID） */
    private Long businessId;
}
