package com.community_shop.backend.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私聊消息详情DTO，用于搭建形同qq，微信登常见社交软件的用户私聊界面
 */
@Data
public class PrivateMessageDetailDTO {

    /** 消息ID */
    private Long messageId;

    /** 完整内容 */
    private String content;

    /** 发送者信息（脱敏） */
    private MessageDetailDTO.SenderDTO sender;

    /** 接收者信息（仅管理员可见） */
    private MessageDetailDTO.ReceiverDTO receiver;

    /** 附件URL列表（数组格式，前端可直接渲染） */
    private List<String> attachments;

    /** 发送时间 */
    private LocalDateTime createTime;

    /**
     * 发送者信息内部类
     */
    @Data
    public static class SenderDTO {
        private Long userId;
        private String username;
        private String avatarUrl;
    }

    /**
     * 接收者信息内部类（管理员专用）
     */
    @Data
    public static class ReceiverDTO {
        private Long userId;
        private String username;
        private String avatarUrl;
    }
}
