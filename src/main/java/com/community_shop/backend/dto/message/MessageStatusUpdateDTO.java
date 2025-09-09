package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息状态更新DTO（匹配MessageService.updateMessageStatus方法）
 */
@Data
public class MessageStatusUpdateDTO {

    /** 消息ID（非空） */
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    /** 操作用户ID（非空，必须是接收者才能更新状态） */
    @NotNull(message = "操作用户ID不能为空")
    private Long operateUserId;

    /** 目标状态（非空，只能更新为READ/DELETED） */
    @NotNull(message = "目标状态不能为空")
    private MessageStatusEnum targetStatus;
}
