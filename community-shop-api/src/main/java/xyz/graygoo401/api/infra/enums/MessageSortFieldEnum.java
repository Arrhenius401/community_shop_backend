package xyz.graygoo401.api.infra.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息排序字段枚举（匹配MessageQueryDTO的排序需求）
 */
@Getter
@AllArgsConstructor
public enum MessageSortFieldEnum {

    /** 发送时间 */
    CREATE_TIME("createTime", "create_time", "发送时间"),

    /** 状态 */
    STATUS("status", "status", "状态（未读优先）");

    @JsonValue
    private final String fieldName;  // 前端传入字段名
    private final String dbField;    // 数据库字段名
    private final String description;

    public static MessageSortFieldEnum getByFieldName(String fieldName) {
        for (MessageSortFieldEnum e : values()) {
            if (e.fieldName.equals(fieldName)) return e;
        }
        return null;
    }
}
