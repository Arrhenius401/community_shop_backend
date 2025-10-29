package com.community_shop.backend.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 跟帖创建请求DTO
 * 用于接收前端发布跟帖的请求参数
 */
@Data
@Schema(description = "跟帖创建请求参数")
public class PostFollowPublishDTO {
    /** 关联帖子ID,非空 */
    @NotNull(message = "关联帖子ID不能为空")
    @Schema(description = "关联的帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /** 跟帖内容，非空，长度1-500字 */
    @NotBlank(message = "跟帖内容不能为空")
    @Size(min = 1, max = 500, message = "跟帖内容需在1-500字之间")
    @Schema(description = "跟帖内容", example = "这个商品真的很不错！", minLength = 1, maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /** 被@的用户ID列表，逗号分隔 */
    @Schema(description = "被@的用户ID列表（逗号分隔）", example = "1001,1002")
    private String atUserIds;
}