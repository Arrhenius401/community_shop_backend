package xyz.graygoo401.common.enums;

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
    @JsonValue
    private final String direction;

    /** 业务描述（用于日志和文档） */
    private final String desc;

}
