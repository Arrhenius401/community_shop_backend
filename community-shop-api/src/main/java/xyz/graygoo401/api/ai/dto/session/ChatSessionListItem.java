package xyz.graygoo401.api.ai.dto.session;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.graygoo401.api.ai.enums.ChatSessionStatusEnum;

import java.time.LocalDateTime;

/**
 * AI 会话列表DTO
 */
@NoArgsConstructor
@Data
@Schema(description = "会话列表数据模型")
public class ChatSessionListItem {

    /** 会话ID */
    @TableId(value = "chat_session_id")
    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "会话唯一标识", example = "SESSION123456")
    private String chatSessionId;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户唯一标识", example = "10001")
    private Long userId;

    /** 会话标题（根据用户的第一个消息给出） */
    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话内容标题", example = "关于感冒症状的咨询")
    private String title;

    /** 创建时间*/
    @Schema(description = "会话创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "会话最后更新时间", example = "2023-10-01T10:30:00")
    private LocalDateTime updateTime;

    /** 会话状态 */
    @Schema(description = "会话状态（枚举）", example = "ACTIVE")
    private ChatSessionStatusEnum status;
}