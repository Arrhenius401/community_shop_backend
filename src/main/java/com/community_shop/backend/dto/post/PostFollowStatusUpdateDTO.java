package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.code.PostFollowStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 跟帖状态更新请求DTO（匹配PostFollowService.updatePostFollowStatus方法）
 */
@Data
@Schema(description = "跟帖状态更新请求参数")
public class PostFollowStatusUpdateDTO {

    /** 跟帖ID */
    @NotNull(message = "跟帖ID不能为空")
    @Schema(description = "跟帖ID", example = "3001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postFollowId;

    /** 目标状态（枚举：NORMAL-正常；HIDDEN-隐藏） */
    @NotNull(message = "目标状态不能为空")
    @Schema(description = "跟帖目标状态", example = "HIDDEN", allowableValues = {"NORMAL", "HIDDEN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private PostFollowStatusEnum targetStatus;
}