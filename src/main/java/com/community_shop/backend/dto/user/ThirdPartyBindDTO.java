package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方账号绑定请求DTO（匹配Service层UserThirdPartyService.bind方法）
 */
@Data
@Schema(description = "第三方账号绑定请求数据模型")
public class ThirdPartyBindDTO {

    /** 第三方平台类型（WECHAT/QQ/ALIPAY，必填） */
    @NotNull(message = "第三方平台类型不能为空")
    @Schema(description = "第三方平台类型", example = "WECHAT")
    private ThirdPartyTypeEnum thirdType;

    /** 第三方OpenID（平台唯一标识，必填） */
    @NotBlank(message = "OpenID不能为空")
    @Schema(description = "第三方平台返回的用户唯一标识", example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M")
    private String openid;

    /** 第三方临时凭证（access_token，用于校验有效性） */
    @NotBlank(message = "临时凭证不能为空")
    @Schema(description = "第三方平台返回的临时访问凭证", example = "access_token123456")
    private String accessToken;
}