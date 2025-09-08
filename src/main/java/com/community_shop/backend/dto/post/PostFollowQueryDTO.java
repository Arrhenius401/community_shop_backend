package com.community_shop.backend.dto.post;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 跟帖列表查询DTO（匹配PostFollowService.selectPostFollowsByPostId方法）
 */
@Data
public class PostFollowQueryDTO extends PageParam {

    /** 帖子ID（查询该帖子的跟帖，非空） */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 跟帖状态（默认查询正常跟帖，枚举：NORMAL-正常；HIDDEN-隐藏） */
    private PostFollowStatusEnum status = PostFollowStatusEnum.NORMAL;
}
