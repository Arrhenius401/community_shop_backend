package com.community_shop.backend.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机验证码请求DTO
 */
@Data
@Schema(description = "验证手机请求数据模型")
public class VerifyPhoneDTO {

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号", example = "17268287727")
    private String phoneNumber;

    /** 验证码 */
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
    @Schema(description = "验证码", example = "123456")
    private String verifyCode;
}
