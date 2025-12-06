package com.community_shop.backend.dto.chat;

import com.community_shop.backend.enums.code.ChatMessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 会话消息详情DTO
 */
@NoArgsConstructor
@Data
@Schema(description = "会话消息详情数据模型")
public class ChatMessageDetailDTO {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "所属会话唯一标识", example = "SESSION123456")
    private String chatSessionId;

    /** 消息角色 */
    @NotNull(message = "消息角色不能为空")
    @Schema(description = "消息发送方类型（枚举）", example = "USER")
    private ChatMessageTypeEnum messageType;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息具体内容", example = "我最近有点咳嗽，该吃什么药？")
    private String content;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP，无需手动设置） */
    @Schema(description = "消息创建时间", example = "2023-10-01T10:15:00")
    private LocalDateTime createTime;
}