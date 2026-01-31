package xyz.graygoo401.api.infra.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私聊消息详情DTO，用于搭建形同qq，微信登常见社交软件的用户私聊界面
 */
@Data
@Schema(description = "私聊消息详情DTO，用于私聊界面展示完整消息")
public class PrivateMessageDetailDTO {

    /** 消息ID */
    @Schema(description = "消息唯一标识", example = "10001")
    private Long messageId;

    /** 完整内容 */
    @Schema(description = "私聊消息完整内容", example = "明天上午10点在社区超市门口取货可以吗？")
    private String content;

    /** 发送者信息（脱敏） */
    @Schema(description = "发送者信息（已脱敏）")
    private SenderDTO sender;

    /** 附件URL列表（数组格式，前端可直接渲染） */
    @Schema(description = "消息附件URL列表", example = "[\"https://example.com/chat-img.jpg\"]")
    private List<String> attachments;

    /** 发送时间 */
    @Schema(description = "消息发送时间", example = "2024-05-20T15:30:00")
    private LocalDateTime createTime;

    /**
     * 发送者信息内部类
     */
    @Data
    @Schema(description = "私聊消息发送者信息")
    public static class SenderDTO {
        /** 发送者用户ID */
        @Schema(description = "发送者用户ID", example = "1001")
        private Long userId;

        /** 昵称 */
        @Schema(description = "发送者用户名", example = "user123")
        private String username;

        /** 头像URL */
        @Schema(description = "发送者头像URL", example = "https://example.com/avatar1.jpg")
        private String avatarUrl;
    }
}