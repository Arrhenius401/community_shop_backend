package com.community_shop.backend.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子发布请求DTO（匹配PostService.publishPost方法）
 */
@Data
@Schema(description = "帖子发布请求参数")
public class PostPublishDTO {

    /** 帖子标题（1-50字，非空） */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 50, message = "标题长度不超过50字")
    @Schema(description = "帖子标题", example = "社区超市新品推荐", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /** 帖子内容（1-2000字，支持富文本，非空） */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "内容长度不超过2000字")
    @Schema(description = "帖子内容（支持富文本）", example = "<p>今天超市到了一批新鲜水果...</p>", maxLength = 2000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /** 帖子图片URL列表（JSON格式，最多9张，可选） */
    @Schema(description = "帖子图片URL列表（JSON数组格式）", example = "[\"https://example.com/img1.jpg\",\"https://example.com/img2.jpg\"]", maxLength = 1000)
    private String imageUrls;
}