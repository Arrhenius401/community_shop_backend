package com.community_shop.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方账号绑定请求DTO（匹配Service层UserThirdPartyService.bind方法）
 */
@Data
public class ThirdPartyBindDTO {

    /** 用户ID（当前登录用户，必填） */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 第三方平台类型（WECHAT/QQ/ALIPAY，必填） */
    @NotBlank(message = "第三方平台类型不能为空")
    private String thirdType;

    /** 第三方OpenID（平台唯一标识，必填） */
    @NotBlank(message = "OpenID不能为空")
    private String openid;

    /** 第三方临时凭证（access_token，用于校验有效性） */
    @NotBlank(message = "临时凭证不能为空")
    private String accessToken;
}
