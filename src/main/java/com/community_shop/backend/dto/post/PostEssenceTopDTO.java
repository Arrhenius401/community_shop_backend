package com.community_shop.backend.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子置顶/加精请求DTO（匹配PostService.setPostEssenceOrTop方法）
 */
@Data
@Schema(description = "帖子置顶/加精状态更新请求参数")
public class PostEssenceTopDTO {

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /** 是否精华（true-是；false-否） */
    @NotNull(message = "精华状态不能为空")
    @Schema(description = "是否设为精华帖", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isEssence;

    /** 是否置顶（true-是；false-否） */
    @NotNull(message = "置顶状态不能为空")
    @Schema(description = "是否设为置顶帖", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isTop;
}