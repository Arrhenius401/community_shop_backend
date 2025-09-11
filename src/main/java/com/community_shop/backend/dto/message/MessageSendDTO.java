package com.community_shop.backend.dto.message;


import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 消息发送请求DTO（匹配MessageService.sendPrivateMessage方法）
 */
@Data
public class MessageSendDTO {

    /** 接收者ID（私信必填，系统通知可为null） */
    private Long receiverId;

    /** 消息内容（1-1000字，非空） */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不超过1000字")
    private String content;

    /** 消息类型（非空，决定展示样式和权限） */
    @NotNull(message = "消息类型不能为空")
    private MessageTypeEnum type;

    /** 关联业务ID（可选，如回复通知关联的帖子ID） */
    private Long businessId;

    /** 附件URL列表（JSON格式，最多3个，如图片/文件） */
    private String attachments;
}
