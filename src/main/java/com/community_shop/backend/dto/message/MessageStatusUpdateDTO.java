package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.code.MessageStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息状态更新DTO（匹配MessageService.updateMessageStatus方法）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息状态更新DTO，用于更新消息的状态（如已读/删除）")
public class MessageStatusUpdateDTO {

    /** 消息ID（非空） */
    @NotNull(message = "消息ID不能为空")
    @Schema(description = "消息唯一标识", example = "10001")
    private Long messageId;

    /** 目标状态（非空，只能更新为READ/DELETED） */
    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态（仅支持READ/DELETED）", example = "READ")
    private MessageStatusEnum targetStatus;
}