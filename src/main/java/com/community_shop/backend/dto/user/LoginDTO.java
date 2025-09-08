package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.SimpleEnum.LoginTypeEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录dto
 */
@Data
public class LoginDTO {

    /**
     * 登录类型（枚举：USERNAME-账号密码登录；PHONE-手机号登录）
     * 依据：系统设计文档“注册登录支持手机号+验证码、邮箱+密码”，需区分登录方式
     */
    @NotBlank(message = "登录类型不能为空")
    private LoginTypeEnum loginType;

    /**
     * 登录标识（用户名/手机号）
     * 依据：Mapper层selectByUsername（用户名）、selectByPhone（手机号）接口，支持双标识登录
     */
    @NotBlank(message = "登录标识不能为空")
    private String loginId;

    /**
     * 登录凭证（密码/验证码）
     * 依据：Service层登录校验逻辑——账号登录需密码，手机号登录需验证码
     */
    @NotBlank(message = "登录凭证不能为空")
    private String credential;

    /**
     * 验证码（仅手机号登录时必填，可选参数）
     * 依据：系统设计文档“手机号+验证码”登录场景，需单独校验验证码
     */
    private String verifyCode;


    // 辅助方法：判断是否为手机号登录（用于Controller层参数适配）
    public boolean isPhoneLogin() {
        return LoginTypeEnum.PHONE_NUMBER.equals(this.loginType);
    }

    // 辅助方法：判断是否为邮箱密码登录（用于Controller层参数适配）
    public boolean isEmailLogin() {
        return LoginTypeEnum.EMAIL.equals(this.loginType);
    }
}
