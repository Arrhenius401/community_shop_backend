package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.SimpleEnum.LoginTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登录dto
 */
@Data
@Schema(description = "用户登录请求数据模型")
public class LoginDTO {

    /** 登录类型（枚举：USERNAME-账号密码登录；PHONE-手机号登录） */
    @NotNull(message = "登录类型不能为空")
    @Schema(description = "登录类型（PHONE_NUMBER-手机号登录，EMAIL-邮箱密码登录）", example = "EMAIL")
    private LoginTypeEnum loginType;

    /** 登录标识（用户名/手机号） */
    @NotBlank(message = "登录标识不能为空")
    @Schema(description = "登录标识（手机号或邮箱）", example = "zhangsan@example.com")
    private String loginId;

    /** 登录凭证（密码/验证码） */
    @NotBlank(message = "登录凭证不能为空")
    @Schema(description = "登录凭证（密码或验证码）", example = "Pass123456")
    private String credential;

    /** 验证码（仅手机号登录时必填，可选参数） */
    @Schema(description = "手机号登录时的验证码（仅手机号登录必填）", example = "123456")
    private String verifyCode;


    /** 辅助方法：判断是否为手机号登录（用于Controller层参数适配） */
    public boolean isPhoneLogin() {
        return LoginTypeEnum.PHONE_NUMBER.equals(this.loginType);
    }

    /** 辅助方法：判断是否为邮箱登录（用于Controller层参数适配） */
    public boolean isEmailLogin() {
        return LoginTypeEnum.EMAIL.equals(this.loginType);
    }
}