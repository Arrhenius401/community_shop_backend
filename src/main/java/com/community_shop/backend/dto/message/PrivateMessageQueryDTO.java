package com.community_shop.backend.dto.message;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 简单消息查询参数，适用于私聊环境
 */
@Data
public class PrivateMessageQueryDTO extends PageParam {

    /** 谈话对象ID */
    @NotNull(message = "当前用户ID不能为空")
    private Long partnerId;

    /** 消息类型（可选，如只看“评论回复”） */
    private MessageTypeEnum type;
}
