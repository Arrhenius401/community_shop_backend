package com.community_shop.backend.enums.SortEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子排序字段枚举（匹配PostQueryDTO的排序需求）
 */
@Getter
@AllArgsConstructor
public enum PostSortFieldEnum {

    CREATE_TIME("createTime", "create_time", "发布时间"),   // Java字段→数据库字段映射
    UPDATE_TIME("updateTime", "update_time", "更新时间"),
    LIKE_COUNT("likeCount", "like_count", "点赞数"),
    COMMENT_COUNT("postFollowCount", "comment_count", "评论数");

    /** 前端传入的参数值（如"viewCount"） */
    @JsonValue
    private final String fieldName;

    /** 对应的数据库字段（如"view_count"，用于Mapper层SQL拼接） */
    private final String dbField;

    /** 业务描述 */
    private final String description;

    public static PostSortFieldEnum getByFieldName(String fieldName) {
        for (PostSortFieldEnum enumItem : values()) {
            if (enumItem.fieldName.equals(fieldName)) {
                return enumItem;
            }
        }
        return null;
    }
}
