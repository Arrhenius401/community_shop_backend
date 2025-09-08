package com.community_shop.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 跟帖创建请求DTO
 * 用于接收前端发布跟帖的请求参数
 */
@Data
public class PostFollowPublishDTO {
    /**
     * 关联帖子ID
     * 约束：非空（必须绑定具体帖子），匹配post表post_id类型
     */
    @NotNull(message = "关联帖子ID不能为空")
    private Long postId;

    /**
     * 创建者ID
     * 约束：非空，匹配user表user_id字段类型
     */
    @NotNull(message = "创建者ID不能为空")
    private Long userId;

    /**
     * 跟帖内容
     * 约束：非空、长度1-500字（防止空内容或刷屏），匹配post_follow表content字段长度
     */
    @NotBlank(message = "跟帖内容不能为空")
    @Size(min = 1, max = 500, message = "跟帖内容需在1-500字之间")
    private String content;

    /**
     * 扩展字段：是否@用户（可选）
     * 格式：多个用户ID用逗号分隔（如"1001,1002"），用于触发@通知业务
     */
    private String atUserIds;
}
