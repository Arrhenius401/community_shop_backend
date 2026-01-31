package xyz.graygoo401.api.infra.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.graygoo401.api.infra.enums.MessageSortFieldEnum;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;
import xyz.graygoo401.common.dto.PageParam;
import xyz.graygoo401.common.enums.SortDirectionEnum;

/**
 * 消息列表查询DTO（匹配MessageService.queryMessageList方法）
 */
@Data
@Schema(description = "消息列表查询DTO，用于分页查询消息列表")
public class MessageQueryDTO extends PageParam {

    /** 接收者ID */
    @Schema(description = "接收者用户ID（可选，默认当前登录用户）", example = "1001")
    private Long receiverId;

    /** 消息类型（可选，如只看“评论回复”） */
    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型（必选过滤条件）", example = "SYSTEM")
    private MessageTypeEnum type;

    /** 是否已读（可选，如只看“未读”消息） */
    @Schema(description = "是否已读（可选过滤条件，null表示全部）", example = "false")
    private Boolean isRead;

    /** 搜索关键词（可选，模糊匹配内容） */
    @Schema(description = "内容搜索关键词（可选，模糊匹配）", example = "订单")
    private String keyword;

    /** 排序字段（默认按时间倒序） */
    @Schema(description = "排序字段（默认CREATE_TIME）", example = "CREATE_TIME")
    private MessageSortFieldEnum sortField = MessageSortFieldEnum.CREATE_TIME;

    /** 排序方向（默认降序） */
    @Schema(description = "排序方向（默认DESC）", example = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}