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

    /** 当前用户ID（非空，用于筛选“发给我的”或“我发的”消息） */
    @NotNull(message = "当前用户ID不能为空")
    private Long userId;

    /** 消息类型（可选，如只看“评论回复”） */
    private MessageTypeEnum type;

    /** 消息状态（可选，如只看“未读”消息） */
    private MessageStatusEnum status;

    /** 搜索关键词（可选，模糊匹配内容） */
    private String keyword;

    /** 排序字段（默认按时间倒序） */
    private MessageSortFieldEnum sortField = MessageSortFieldEnum.CREATE_TIME;

    /** 排序方向（默认降序） */
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
