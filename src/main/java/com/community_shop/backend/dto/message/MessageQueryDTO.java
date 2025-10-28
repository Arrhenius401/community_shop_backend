package com.community_shop.backend.dto.message;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.enums.SortEnum.MessageSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息列表查询DTO（匹配MessageService.queryMessageList方法）
 */
@Data
public class MessageQueryDTO extends PageParam {

    /** 接收者ID */
    private Long receiverId;

    /** 消息类型（可选，如只看“评论回复”） */
    @NotNull(message = "消息类型不能为空")
    private MessageTypeEnum type;

    /** 是否已读（可选，如只看“未读”消息） */
    private Boolean isRead;

    /** 搜索关键词（可选，模糊匹配内容） */
    private String keyword;

    /** 排序字段（默认按时间倒序） */
    private MessageSortFieldEnum sortField = MessageSortFieldEnum.CREATE_TIME;

    /** 排序方向（默认降序） */
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
