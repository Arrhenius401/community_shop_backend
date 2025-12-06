package com.community_shop.backend.dto.message;

import com.community_shop.backend.enums.code.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 消息发送请求DTO（匹配MessageService.sendPrivateMessage方法）
 */
@Data
@Schema(description = "消息发送请求DTO，用于提交发送消息的参数")
public class MessageSendDTO {

    /** 接收者ID（私信必填，系统通知可为null） */
    @Schema(description = "接收者用户ID（私信必填，系统通知可为null）", example = "1002")
    private Long receiverId;

    /** 标题（可为空） */
    @Schema(description = "消息标题（可选）", example = "关于订单的疑问")
    private String title;

    /** 消息内容（1-1000字，非空） */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不超过1000字")
    @Schema(description = "消息正文内容（1-1000字）", example = "请问我的订单什么时候发货？", maxLength = 1000)
    private String content;

    /** 消息类型（非空，决定展示样式和权限） */
    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型（决定展示样式和权限）", example = "PRIVATE")
    private MessageTypeEnum type;

    /** 关联业务ID（可选，如回复通知关联的帖子ID） */
    @Schema(description = "关联业务ID（如帖子ID、订单ID，可选）", example = "5001")
    private Long businessId;

    /** 附件URL列表（JSON格式，最多3个，如图片/文件） */
    @Schema(description = "附件URL列表（JSON格式字符串，最多3个）", example = "[\"https://example.com/img1.jpg\"]")
    private String attachments;
}