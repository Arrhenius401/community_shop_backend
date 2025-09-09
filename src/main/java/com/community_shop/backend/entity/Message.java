package com.community_shop.backend.entity;

import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类，对应数据库message表
 */
@Data
public class Message {
    // 消息ID（主键）
    private Long msgId;

    // 发送者ID（0表示系统）
    private Long senderId;

    // 接收者ID（-1表示所有用户）
    private Long receiverId;

    // 消息标题
    private String title;

    // 消息内容
    private String content;

    // 关联订单ID（可为null）
    private Long orderId;

    // 阅读状态（0=未读，1=已读）
    private Boolean isRead;

    // 删除状态（0=未删除，1=已删除）
    private Boolean isDeleted;

    // 消息类型（SYSTEM=系统消息，ORDER=订单消息）
    private MessageTypeEnum type;

    // 消息状态
    private MessageStatusEnum status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    public Message(){}

}
