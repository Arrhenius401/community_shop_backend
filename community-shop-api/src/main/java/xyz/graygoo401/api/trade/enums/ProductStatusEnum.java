package xyz.graygoo401.api.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@AllArgsConstructor
@Getter
public enum ProductStatusEnum {

    /** 新发布商品（尤其新用户发布），需管理员预审（参考帖子预审逻辑） */
    PENDING("PENDING", "待审核"),

    /** 商品审核通过，可售 */
    ON_SALE("ON_SALE", "在售"),

    /** 商品已售罄 */
    SOLD_OUT("SOLD_OUT", "已售罄"),

    /** 商品已下架 */
    OFF_SHELF("OFF_SHELF", "已下架"),

    /** 商品已隐藏 */
    HIDDEN("HIDDEN", "隐藏"),

    /** 商品被封禁 */
    BLOCKED("BLOCKED", "封禁"),

    /** 商品已删除 */
    DELETED("DELETED", "已删除"); // 卖家删除商品（逻辑删除，保留交易关联数据）

    @EnumValue
    private final String code; // 用于数据库存储（varchar类型）
    @Getter
    private final String desc; // 用于前端展示、商品列表筛选

    /**
     * 辅助方法：根据数据库存储的code反向获取枚举对象
     */
    public static ProductStatusEnum getByCode(String code) {
        for (ProductStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
