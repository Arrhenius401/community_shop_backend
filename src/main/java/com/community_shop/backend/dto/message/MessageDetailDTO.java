package com.community_shop.backend.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息详情DTO（匹配MessageService.getMessageDetail方法）
 */
@Data
@Schema(description = "消息详情DTO，包含消息完整信息")
public class MessageDetailDTO {

    /** 消息ID */
    @Schema(description = "消息唯一标识", example = "10001")
    private Long messageId;

    /** 完整内容 */
    @Schema(description = "消息完整文本内容", example = "您的订单已发货，请注意查收")
    private String content;

    /** 发送者信息（脱敏） */
    @Schema(description = "发送者信息（已脱敏）")
    private SenderDTO sender;

    /** 接收者信息（仅管理员可见） */
    @Schema(description = "接收者信息（仅管理员可见）")
    private ReceiverDTO receiver;

    /** 附件URL列表（数组格式，前端可直接渲染） */
    @Schema(description = "消息附件URL列表", example = "[\"https://example.com/file1.jpg\", \"https://example.com/file2.pdf\"]")
    private List<String> attachments;

    /** 关联业务信息（如回复的帖子标题） */
    @Schema(description = "关联的业务信息（如帖子、订单等）")
    private BusinessInfoDTO businessInfo;

    /** 消息状态 */
    @Schema(description = "消息是否已读", example = "false")
    private Boolean isRead;

    /** 发送时间 */
    @Schema(description = "消息发送时间", example = "2024-05-20T15:30:00")
    private LocalDateTime createTime;

    /**
     * 发送者信息内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "消息发送者信息（脱敏）")
    public static class SenderDTO {
        /** 发送者用户ID */
        @Schema(description = "发送者用户ID", example = "1001")
        private Long userId;

        /** 昵称（脱敏） */
        @Schema(description = "发送者用户名", example = "user123")
        private String username;

        /** 头像URL（脱敏） */
        @Schema(description = "发送者头像URL", example = "https://example.com/avatar1.jpg")
        private String avatarUrl;
    }

    /**
     * 接收者信息内部类（管理员专用）
     */
    @Data
    @AllArgsConstructor
    @Schema(description = "消息接收者信息（仅管理员可见）")
    public static class ReceiverDTO {
        /** 接收者用户ID */
        @Schema(description = "接收者用户ID", example = "1002")
        private Long userId;

        /** 昵称（脱敏） */
        @Schema(description = "接收者用户名", example = "user456")
        private String username;
    }

    /**
     * 关联业务信息内部类
     */
    @Data
    @Schema(description = "消息关联的业务信息")
    public static class BusinessInfoDTO {
        /** 关联业务ID */
        @Schema(description = "关联业务ID（如帖子ID、订单ID）", example = "5001")
        private Long businessId;

        /** 业务标题 */
        @Schema(description = "业务标题（如帖子标题）", example = "社区团购活动通知")
        private String title;

        /** 业务详情页跳转链接 */
        @Schema(description = "业务详情页跳转链接", example = "https://example.com/post/5001")
        private String url;
    }
}
