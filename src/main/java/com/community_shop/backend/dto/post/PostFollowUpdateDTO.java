package com.community_shop.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 跟帖更新请求VO
 * 用于接收前端编辑跟帖的请求参数
 */
@Data
public class PostFollowUpdateDTO {

    /**
     * 跟帖ID
     * 约束：非空（必须指定具体跟帖），匹配post_follow表post_follow_id类型
     */
    @NotNull(message = "跟帖ID不能为空")
    private Long postFollowId;

    /** 操作人ID（非空，用于权限校验） */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /**
     * 新跟帖内容
     * 约束：非空、长度1-500字（同创建规则），匹配post_follow表content字段长度
     */
    @NotBlank(message = "跟帖内容不能为空")
    @Size(min = 1, max = 500, message = "跟帖内容需在1-500字之间")
    private String newContent;

    /**
     * 扩展字段：是否@用户（可选）
     * 格式：多个用户ID用逗号分隔（如"1001,1002"），用于触发@通知业务
     */
    private String atUserIds;
}
