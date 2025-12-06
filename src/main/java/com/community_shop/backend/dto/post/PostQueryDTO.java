package com.community_shop.backend.dto.post;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.code.PostStatusEnum;
import com.community_shop.backend.enums.sort.PostSortFieldEnum;
import com.community_shop.backend.enums.sort.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子列表查询DTO（匹配PostService分页查询方法）
 */
@Data
@Schema(description = "帖子列表查询参数")
public class PostQueryDTO extends PageParam {

    /** 搜索关键词（模糊匹配标题/内容） */
    @Schema(description = "搜索关键词（模糊匹配标题/内容）", example = "超市优惠")
    private String keyword;

    /** 用户ID */
    @Schema(description = "发布者用户ID（用于查询指定用户的帖子）", example = "2001")
    private Long userId;

    /** 帖子状态 */
    @Schema(description = "帖子状态筛选")
    private PostStatusEnum status;

    /** 是否为热门帖 */
    @Schema(description = "是否筛选热门帖", example = "true")
    private Boolean isHot;

    /** 是否为置顶帖 */
    @Schema(description = "是否筛选置顶帖", example = "false")
    private Boolean isTop;

    /** 是否为精华帖 */
    @Schema(description = "是否筛选精华帖", example = "true")
    private Boolean isEssence;

    /** 排序字段（枚举：CREATE_TIME-发布时间；LIKE_COUNT-点赞数；COMMENT_COUNT-评论数） */
    @NotNull(message = "排序字段不能为空")
    @Schema(description = "排序字段", example = "CREATE_TIME", requiredMode = Schema.RequiredMode.REQUIRED)
    private PostSortFieldEnum sortField = PostSortFieldEnum.CREATE_TIME;

    /** 排序方向（枚举：ASC-升序；DESC-降序） */
    @NotNull(message = "排序方向不能为空")
    @Schema(description = "排序方向", example = "DESC", requiredMode = Schema.RequiredMode.REQUIRED)
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}