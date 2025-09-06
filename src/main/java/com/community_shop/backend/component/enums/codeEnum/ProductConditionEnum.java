package com.community_shop.backend.component.enums.codeEnum;

public enum ProductConditionEnum {
    // 枚举值：名称（数据库存储码值，前端显示名称）
    NEW("NEW", "全新"),                  // 未拆封/未使用
    NINETY_FIVE_PERCENT_NEW("NINETY_FIVE_PERCENT_NEW", "95成新"), // 轻微使用痕迹，功能完好
    NINETY_PERCENT_NEW("NINETY_PERCENT_NEW", "9成新"),     // 明显使用痕迹，无损坏
    EIGHTY_PERCENT_NEW("EIGHTY_PERCENT_NEW", "8成新"),     // 较多使用痕迹，无功能故障
    SEVENTY_PERCENT_NEW("SEVENTY_PERCENT_NEW", "7成新"),     // 明显磨损，功能正常
    USED("USED", "闲置");                  // 长期使用，无核心故障

    private final String code;
    private final String desc;

    // 构造器
    ProductConditionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static ProductConditionEnum getByCode(String code) {
        for (ProductConditionEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的商品成色状态code：" + code);
    }
}
