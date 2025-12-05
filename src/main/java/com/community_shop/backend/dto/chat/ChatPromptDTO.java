package com.community_shop.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 会话 prompt
 */
@NoArgsConstructor
@Data
@Schema(description = "会话提示信息数据模型")
public class ChatPromptDTO {

    /** 会话内容 */
    @NotBlank(message = "会话内容不能为空")
    @Schema(description = "用户输入的会话内容", example = "请告诉我感冒的常见症状")
    private String prompt;

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "所属会话唯一标识", example = "SESSION123456")
    private String chatSessionId;

    /** 用户ID */
    private String userId;
}