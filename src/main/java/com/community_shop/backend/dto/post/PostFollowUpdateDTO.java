package com.community_shop.backend.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 跟帖更新请求VO
 * 用于接收前端编辑跟帖的请求参数
 */
@Data
@Schema(description = "跟帖更新请求参数")
public class PostFollowUpdateDTO {

    /** 跟帖ID,非空 */
    @NotNull(message = "跟帖ID不能为空")
    @Schema(description = "跟帖ID", example = "3001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postFollowId;

    /** 新的跟帖内容,非空 */
    @NotBlank(message = "跟帖内容不能为空")
    @Size(min = 1, max = 500, message = "跟帖内容需在1-500字之间")
    @Schema(description = "更新后的跟帖内容", example = "这个商品真的很不错，性价比很高！", minLength = 1, maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    private String newContent;

    /** 被@的用户ID列表,逗号分隔 */
    @Schema(description = "被@的用户ID列表（逗号分隔）", example = "1001,1003")
    private String atUserIds;
}