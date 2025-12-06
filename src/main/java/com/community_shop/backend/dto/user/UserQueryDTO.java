package com.community_shop.backend.dto.user;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.enums.code.UserStatusEnum;
import com.community_shop.backend.enums.filter.UserFilterFieldEnum;
import com.community_shop.backend.enums.sort.SortDirectionEnum;
import com.community_shop.backend.enums.sort.UserSortFieldEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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

    /** 筛选字段 */
    @Schema(description = "筛选字段（如USERNAME-用户名）", example = "USERNAME")
    private UserFilterFieldEnum filterField;

    /** 筛选值 */
    @Schema(description = "筛选字段对应的值", example = "zhang")
    private String filterValue;

    /** 排序字段（枚举：initTime-发布时间；createTime-价格） */
    @Schema(description = "排序字段", example = "CREATE_TIME")
    private UserSortFieldEnum sortField = UserSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    @Schema(description = "排序方向", example = "DESC")
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}