package xyz.graygoo401.api.infra.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.graygoo401.api.infra.enums.MessageStatusEnum;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;

import java.time.LocalDateTime;

/**
 * 消息列表项DTO（配合PageResult使用，用于消息中心列表页）
 */
@Data
@Schema(description = "消息列表项DTO，用于消息中心列表页展示")
public class MessageListItemDTO {

    /** 消息ID */
    @Schema(description = "消息唯一标识", example = "10001")
    private Long messageId;

    /** 内容摘要（前30字，适配列表展示） */
    @Schema(description = "消息内容摘要（前30字）", example = "您的订单已发货，请注意查收...")
    private String contentSummary;

    /** 发送者信息（脱敏） */
    @Schema(description = "发送者信息（已脱敏）")
    private Sender sender;

    /** 消息类型（用于展示不同图标） */
    @Schema(description = "消息类型（用于展示对应图标）", example = "ORDER")
    private MessageTypeEnum type;

    /** 消息状态（用于显示未读红点） */
    @Schema(description = "消息状态（未读/已读/删除等）", example = "UNREAD")
    private MessageStatusEnum status;

    /** 发送时间 */
    @Schema(description = "消息发送时间", example = "2024-05-20T15:30:00")
    private LocalDateTime createTime;

    /** 是否有附件（用于显示附件图标） */
    @Schema(description = "是否包含附件（用于显示附件图标）", example = "true")
    private Boolean hasAttachment;

    /**
     * 发送者信息内部类（脱敏）
     */
    @Data
    @Schema(description = "消息发送者信息（脱敏）")
    public static class Sender {
        /** 发送者用户ID（系统消息为固定值） */
        @Schema(description = "发送者用户ID（系统消息为固定值）", example = "1001")
        private Long userId;

        /** 昵称（系统消息显示“系统通知”） */
        @Schema(description = "发送者昵称（系统消息显示“系统通知”）", example = "user123")
        private String username;

        /** 头像URL（系统消息用系统图标） */
        @Schema(description = "发送者头像（系统消息用系统图标）", example = "https://example.com/avatar1.jpg")
        private String avatarUrl;
    }
}