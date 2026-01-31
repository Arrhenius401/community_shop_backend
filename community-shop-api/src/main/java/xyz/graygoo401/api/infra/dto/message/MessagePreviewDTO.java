package xyz.graygoo401.api.infra.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;

import java.time.LocalDateTime;

/**
 * 聊天消息预览信息，用于展示用户最近 3 条未读消息的精简信息
 */
@Data
@Schema(description = "消息预览DTO，展示最近未读消息的精简信息")
public class MessagePreviewDTO {

    /** 消息ID */
    @Schema(description = "消息唯一标识", example = "10001")
    private Long messageId;

    /** 发送者名称 */
    @Schema(description = "消息发送者名称", example = "系统通知")
    private String senderName;

    /** 消息内容摘要 */
    @Schema(description = "消息内容摘要（精简展示）", example = "您有一条新的订单通知...")
    private String contentSummary;

    /** 消息类型 */
    @Schema(description = "消息类型", example = "ORDER")
    private MessageTypeEnum type;

    /** 发送时间 */
    @Schema(description = "消息发送时间", example = "2024-05-20T15:30:00")
    private LocalDateTime createTime;

    /** 业务ID（如订单ID） */
    @Schema(description = "关联业务ID（如订单ID）", example = "5001")
    private Long businessId;
}