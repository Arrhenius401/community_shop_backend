package com.community_shop.backend.dto.post;

import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 跟帖状态更新请求DTO（匹配PostFollowService.updatePostFollowStatus方法）
 */
@Data
public class PostFollowStatusUpdateDTO {

    /** 跟帖ID */
    @NotNull(message = "跟帖ID不能为空")
    private Long postFollowId;

    /** 目标状态（枚举：NORMAL-正常；HIDDEN-隐藏） */
    @NotNull(message = "目标状态不能为空")
    private PostFollowStatusEnum targetStatus;
}
