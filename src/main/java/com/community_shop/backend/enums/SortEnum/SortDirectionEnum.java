package com.community_shop.backend.enums.SortEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排序方向枚举（适配所有模块的升序/降序需求）
 */
@Getter
@AllArgsConstructor
public enum SortDirectionEnum {

    ASC("asc", "升序"),
    DESC("desc", "降序");

    /** 前端传入的参数值（如"asc"） */
    @JsonValue // 序列化时返回该值，适配前端参数
    private final String direction;

    /** 业务描述（用于日志和文档） */
    private final String desc;

    /**
     * 根据前端传入的字符串匹配枚举（参数校验用）
     */
    public static SortDirectionEnum getByDirection(String direction) {
        for (SortDirectionEnum enumItem : values()) {
            if (enumItem.direction.equalsIgnoreCase(direction)) {
                return enumItem;
            }
        }
        return null; // 非法值返回null，由校验器拦截
    }
}
