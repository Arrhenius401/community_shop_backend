package com.community_shop.backend.dto.post;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import com.community_shop.backend.enums.SortEnum.ProductSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 跟帖列表查询DTO（匹配PostFollowService.selectPostFollowsByPostId方法）
 */
@Data
@Schema(description = "跟帖列表查询参数")
public class PostFollowQueryDTO extends PageParam {

    /** 帖子ID（查询该帖子的跟帖，非空） */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "关联的帖子ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    /** 跟帖状态（默认查询正常跟帖，枚举：NORMAL-正常；HIDDEN-隐藏） */
    @Schema(description = "跟帖状态筛选", example = "NORMAL")
    private PostFollowStatusEnum status = PostFollowStatusEnum.NORMAL;

    /** 排序字段（枚举：createTime-发布时间） */
    @Schema(description = "排序字段", example = "CREATE_TIME")
    private ProductSortFieldEnum sortField = ProductSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    @Schema(description = "排序方向", example = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}