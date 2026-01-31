package xyz.graygoo401.api.ai.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.common.dto.PageParam;

/**
 * AI 会话查询参数DTO
 */
@NoArgsConstructor
@Data
@Schema(description = "会话查询参数数据模型")
public class ChatSessionQueryDTO extends PageParam {

    /** 用户ID */
    @Schema(description = "用户唯一标识（用于查询指定用户的会话）", example = "10001")
    private Long userId;
}