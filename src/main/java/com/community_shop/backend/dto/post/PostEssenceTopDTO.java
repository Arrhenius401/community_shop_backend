package com.community_shop.backend.dto.post;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子置顶/加精请求DTO（匹配PostService.setPostEssenceOrTop方法）
 */
@Data
public class PostEssenceTopDTO {

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 是否精华（true-是；false-否） */
    @NotNull(message = "精华状态不能为空")
    private Boolean isEssence;

    /** 是否置顶（true-是；false-否） */
    @NotNull(message = "置顶状态不能为空")
    private Boolean isTop;

}
