package com.community_shop.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子发布请求DTO（匹配PostService.publishPost方法）
 */
@Data
public class PostPublishDTO {

    /** 发帖人ID（当前登录用户，非空，用于校验信用分） */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 帖子标题（1-50字，非空） */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 50, message = "标题长度不超过50字")
    private String title;

    /** 帖子内容（1-2000字，支持富文本，非空） */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "内容长度不超过2000字")
    private String content;

    /** 帖子图片URL列表（JSON格式，最多9张，可选） */
    private String imageUrls;
}
