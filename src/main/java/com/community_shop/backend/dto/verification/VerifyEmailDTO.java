package com.community_shop.backend.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 邮箱验证码请求DTO
 */
@Data
@Schema(description = "验证邮箱请求数据模型")
public class VerifyEmailDTO {

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /** 邮箱（标准格式，登录/找回密码场景） */
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式错误")
    @Schema(description = "邮箱", example = "1825270596@qq.com")
    private String email;

    /** 验证码（6位数字，登录/找回密码场景） */
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
    @Schema(description = "验证码", example = "123456")
    private String verifyCode;
}
