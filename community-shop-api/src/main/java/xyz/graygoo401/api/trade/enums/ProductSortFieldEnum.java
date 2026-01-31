package xyz.graygoo401.api.trade.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品模块排序字段枚举（匹配代码文档2 ProductService的排序需求）
 */
@Getter
@AllArgsConstructor
public enum ProductSortFieldEnum {

    /** 浏览量 */
    VIEW_COUNT("viewCount", "view_count", "浏览量"),

    /** 发布时间 */
    CREATE_TIME("createTime", "create_time", "发布时间"),

    /** 价格 */
    PRICE("price", "price", "价格");

    /** 前端传入的参数值（如"viewCount"） */
    @JsonValue
    private final String fieldName;

    /** 对应的数据库字段（如"view_count"，用于Mapper层SQL拼接） */
    private final String dbField;

    /** 业务描述 */
    private final String description;

}
