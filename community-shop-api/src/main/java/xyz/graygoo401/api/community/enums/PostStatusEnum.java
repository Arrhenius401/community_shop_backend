package xyz.graygoo401.api.community.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子状态枚举类
 */
@AllArgsConstructor
@Getter
public enum PostStatusEnum {

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
    private final String code;

    private final String desc;

    /**
     * 辅助方法：根据code反向获取枚举对象
     */
    public static PostStatusEnum getByCode(String code) {
        for (PostStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
