package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息详情DTO（匹配MessageService.getMessageDetail方法）
 */
@Data
public class MessageDetailDTO {

    /** 消息ID */
    private Long messageId;

    /** 完整内容 */
    private String content;

    /** 发送者信息（脱敏） */
    private SenderDTO sender;

    /** 接收者信息（仅管理员可见） */
    private ReceiverDTO receiver;

    /** 附件URL列表（数组格式，前端可直接渲染） */
    private List<String> attachments;

    /** 关联业务信息（如回复的帖子标题） */
    private BusinessInfoDTO businessInfo;

    /** 消息状态 */
    private MessageStatusEnum status;

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
    }

    /**
     * 关联业务信息内部类
     */
    @Data
    public static class BusinessInfoDTO {
        private Long businessId;   // 关联业务ID（如帖子ID）
        private String title;      // 业务标题（如帖子标题）
        private String url;        // 跳转链接（如帖子详情页URL）
    }
}
