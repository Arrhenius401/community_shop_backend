package com.community_shop.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方登录请求DTO（匹配Service层loginByThirdParty方法）
 */
@Data
public class ThirdPartyLoginDTO {

    /** 第三方平台类型（枚举：WECHAT/QQ/ALIPAY，匹配ThirdPartyTypeEnum） */
    @NotBlank(message = "第三方平台类型不能为空")
    private String thirdType;

    /** 第三方授权码（用于获取OpenID和access_token） */
    @NotBlank(message = "授权码不能为空")
    private String authCode;
}
