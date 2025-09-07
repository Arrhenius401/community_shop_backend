package com.community_shop.backend.vo.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子更新VO：封装用户更新帖子时可修改的字段
 * 仅包含用户有权限修改的内容，不包含状态、点赞数等系统维护字段
 */
@Data
public class PostUpdateVO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId; // 必须携带，用于定位要更新的帖子

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title; // 允许修改标题

    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    private String content; // 允许修改内容


}
