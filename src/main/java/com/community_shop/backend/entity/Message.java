package com.community_shop.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类，对应数据库message表
 */
@Data
@TableName("message")
public class Message {

    /** 消息ID */
    @TableId(value = "msg_id", type = IdType.AUTO)
    private Long msgId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 关联的订单ID */
    private Long orderId;

    /** 是否已读 */
    private Boolean isRead;

    /** 是否已删除 */
    private Boolean isDeleted;

    /** 消息类型 */
    private MessageTypeEnum type;

    /** 消息状态 */
    private MessageStatusEnum status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    public Message(){}

}
