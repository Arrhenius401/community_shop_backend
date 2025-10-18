package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子状态更新请求DTO（匹配PostService.updatePostStatus方法）
 */
@Data
public class PostStatusUpdateDTO {

    /** 帖子ID（非空，用于定位帖子） */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 目标状态（非空，需为PostStatusEnum中的合法值） */
    @NotBlank(message = "目标状态不能为空")
    private PostStatusEnum status;

    /** 操作人ID（非空，用于权限校验） */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /** 状态变更原因（可选，如驳回原因、删除原因，最多200字） */
    private String reason;
}
