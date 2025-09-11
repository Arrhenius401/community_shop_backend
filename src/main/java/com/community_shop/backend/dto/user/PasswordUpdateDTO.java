package com.community_shop.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 密码修改请求DTO
 */
@Data
public class PasswordUpdateDTO {

    /** 原密码（校验身份） */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码（6-20位，含字母和数字） */
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$", message = "新密码需含字母和数字，长度6-20位")
    private String newPassword;

    /** 确认新密码（需与新密码一致） */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
