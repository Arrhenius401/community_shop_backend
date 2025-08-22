package com.community_shop.backend.entity.enums.statusEnum;

public enum UserStatusEnum {

    ADMIN("ADMIN", "管理员"),  //管理员，有操作台
    NORMAL("NORMAL", "正常状态"), // 信用分≥80分，无违规，权限完整（可发帖/发布商品/交易）
    LIMITED_TRADE("LIMITED_TRADE", "交易限制"), // 信用分60-79分，仅可浏览/购买商品，不可发布商品
    BANNED_TEMP("BANNED_TEMP", "临时封禁"), // 违规后被管理员临时封禁（按时长，如7天/30天），不可发帖/交易
    BANNED_PERMANENT("BANNED_PERMANENT", "永久封禁"), // 严重违规（如多次虚假交易），账号永久冻结
    INACTIVE("INACTIVE", "未激活"), // 新注册用户未完成手机号/邮箱验证，仅可登录，不可发帖/交易
    DELETED("DELETED", "删除");    //逻辑删除状态

    private final String code; // 用于数据库存储（varchar类型）
    private final String desc; // 用于前端展示、权限校验说明

    // 构造器
    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // Getter方法
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 辅助方法：根据code反向获取枚举对象
    public static UserStatusEnum getByCode(String code) {
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户状态code：" + code);
    }
}
