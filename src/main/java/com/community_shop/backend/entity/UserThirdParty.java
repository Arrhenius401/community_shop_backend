package com.community_shop.backend.entity;

import com.community_shop.backend.enums.simpleEnum.ThirdPartyTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方账号关联实体
 * 对应数据库表：user_third_party
 */
@Data
public class UserThirdParty {
    /** 自增主键 */
    private Long id;
    /** 关联平台用户ID（外键关联user.user_id） */
    private Long userId;
    /** 第三方平台类型（WECHAT/QQ/ALIPAY） */
    private ThirdPartyTypeEnum thirdType;
    /** 第三方平台唯一标识（如微信openid） */
    private String openid;
    /** 第三方临时凭证（加密存储） */
    private String accessToken;
    /** 绑定时间 */
    private LocalDateTime bindTime;
    /** 绑定状态（1-有效，0-已解绑） */
    private Integer isValid;

    /**
     * 无参构造函数
     */
    public UserThirdParty() {}

    /**
     * 全参构造函数
     * @param userId 关联平台用户ID
     * @param thirdType 第三方平台类型
     * @param openid 第三方平台唯一标识
     * @param accessToken 第三方临时凭证
     */
    public UserThirdParty(Long userId, ThirdPartyTypeEnum thirdType, String openid, String accessToken) {
        this.userId = userId;
        this.thirdType = thirdType;
        this.openid = openid;
        this.accessToken = accessToken;
        this.bindTime = LocalDateTime.now();
        this.isValid = 1;
    }

    public UserThirdParty(Long id, Long userId, ThirdPartyTypeEnum thirdType, String openid, String accessToken, LocalDateTime bindTime, Integer isValid) {
        this.id = id;
        this.userId = userId;
        this.thirdType = thirdType;
        this.openid = openid;
        this.accessToken = accessToken;
        this.bindTime = bindTime;
        this.isValid = isValid;
    }
}
