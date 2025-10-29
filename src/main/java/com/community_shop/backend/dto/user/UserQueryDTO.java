package com.community_shop.backend.dto.user;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.enums.SortEnum.ProductSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户列表查询DTO（适配Mapper层多条件查询接口）
 */
@Data
@Schema(description = "用户列表查询请求数据模型")
public class UserQueryDTO extends PageParam {

    /** 用户状态 */
    @Schema(description = "用户状态筛选（NORMAL-正常，DISABLED-禁用）", example = "NORMAL")
    private UserStatusEnum status;

    /** 用户角色 */
    @Schema(description = "用户角色筛选（USER-普通用户，ADMIN-管理员）", example = "USER")
    private UserRoleEnum role;

    /** 兴趣标签列表（匹配selectByInterestTags接口） */
    @Schema(description = "兴趣标签筛选列表", example = "[\"读书\", \"运动\"]")
    private List<String> interestTags;

    /** 筛选字段（如“creditScore”“postCount”，匹配getUsersByAllParam的compareIndex） */
    @Schema(description = "筛选字段（如creditScore-信用分，postCount-发帖数）", example = "creditScore")
    private String filterField;

    /** 筛选值（如“80”，匹配getUsersByAllParam的compareParam） */
    @Schema(description = "筛选字段对应的值", example = "80")
    private String filterValue;

    /** 排序字段（枚举：initTime-发布时间；createTime-价格） */
    @Schema(description = "排序字段", example = "CREATE_TIME")
    private ProductSortFieldEnum sortField = ProductSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    @Schema(description = "排序方向", example = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}