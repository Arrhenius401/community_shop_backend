package com.community_shop.backend.enums.SortEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块排序字段枚举（匹配代码文档2 ProductService的排序需求）
 */
@Getter
@AllArgsConstructor
public enum UserSortFieldEnum {

    CREDIT_SCORE("creditScore", "credit_score", "信用分"), // Java字段→数据库字段映射
    CREATE_TIME("createTime", "create_time", "创建时间"),
    POST_COUNT("postCount", "post_count", "发帖数"),
    FOLLOWER_COUNT("followerCount", "follower_count", "粉丝数");

    /** 前端传入的参数值（如"viewCount"） */
    @JsonValue
    private final String fieldName;

    /** 对应的数据库字段（如"view_count"，用于Mapper层SQL拼接） */
    private final String dbField;

    /** 业务描述 */
    private final String description;

    /**
     * 根据前端传入的字符串匹配枚举（参数校验用）
     */
    public static UserSortFieldEnum getByFieldName(String fieldName) {
        for (UserSortFieldEnum enumItem : values()) {
            if (enumItem.fieldName.equals(fieldName)) {
                return enumItem;
            }
        }
        return null; // 非法值返回null，由校验器拦截
    }
}
