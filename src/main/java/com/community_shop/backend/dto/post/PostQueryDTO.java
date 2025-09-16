package com.community_shop.backend.dto.post;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.PostStatusEnum;
import com.community_shop.backend.enums.SortEnum.PostSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子列表查询DTO（匹配PostService分页查询方法）
 */
@Data
public class PostQueryDTO extends PageParam {

    /** 搜索关键词（模糊匹配标题/内容） */
    private String keyword;

    /** 用户ID */
    private Long userId;

    /** 帖子状态 */
    private PostStatusEnum status;

    /** 排序字段（枚举：CREATE_TIME-发布时间；LIKE_COUNT-点赞数；COMMENT_COUNT-评论数） */
    @NotNull(message = "排序字段不能为空")
    private PostSortFieldEnum sortField = PostSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序） */
    @NotNull(message = "排序方向不能为空")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
