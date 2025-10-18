package com.community_shop.backend.dto.user;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.enums.SortEnum.ProductSortFieldEnum;
import com.community_shop.backend.enums.SortEnum.SortDirectionEnum;
import lombok.Data;

import java.util.List;

/**
 * 用户列表查询DTO（适配Mapper层多条件查询接口）
 */
@Data
public class UserQueryDTO extends PageParam {

    /** 用户状态 */
    private UserStatusEnum status;

    /** 用户角色 */
    private UserRoleEnum role;

    /** 兴趣标签列表（匹配selectByInterestTags接口） */
    private List<String> interestTags;

    /** 筛选字段（如“creditScore”“postCount”，匹配getUsersByAllParam的compareIndex） */
    private String filterField;

    /** 筛选值（如“80”，匹配getUsersByAllParam的compareParam） */
    private String filterValue;

    /** 排序字段（枚举：initTime-发布时间；createTime-价格） */
    private ProductSortFieldEnum sortField = ProductSortFieldEnum.CREATE_TIME;

    /** 排序方向（asc-升序；desc-降序，默认降序） */
    private SortDirectionEnum sortDir = SortDirectionEnum.DESC;
}
