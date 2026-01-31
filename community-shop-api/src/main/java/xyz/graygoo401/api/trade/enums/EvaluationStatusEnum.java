package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价状态枚举
 */
@Getter
@AllArgsConstructor
public enum EvaluationStatusEnum {

    /** 草稿 */
    DRAFT("DRAFT", "草稿"),

    /** 待审核 */
    PENDING("PENDING", "待审核"),

    /** 正常 */
    NORMAL("NORMAL", "正常"),

    /** 隐藏 */
    HIDDEN("HIDDEN", "隐藏"),

    /** 封禁 */
    BLOCKED("BLOCKED", "封禁"),

    /** 已删除 */
    DELETED("DELETED", "已删除");

    @JsonValue
    @EnumValue
    private final String code; // 用于数据库存储（varchar类型）

    private final String desc;

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static EvaluationStatusEnum getByCode(String code) {
        for (EvaluationStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
