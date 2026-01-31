package xyz.graygoo401.api.infra.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;
import xyz.graygoo401.common.dto.PageParam;

/**
 * 简单消息查询参数，适用于私聊环境
 */
@Data
@Schema(description = "私聊消息查询参数DTO")
public class PrivateMessageQueryDTO extends PageParam {

    /** 当前用户ID */
    @NotNull(message = "当前用户ID不能为空")
    @Schema(description = "当前用户ID", example = "1001")
    private Long userId;

    /** 谈话对象ID */
    @NotNull(message = "谈话对象ID不能为空")
    @Schema(description = "聊天对象用户ID", example = "1002")
    private Long partnerId;

    /** 消息类型（可选，如只看“评论回复”） */
    @Schema(description = "消息类型（可选过滤条件）", example = "PRIVATE")
    private MessageTypeEnum type;
}