package com.community_shop.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 密码修改请求DTO
 */
@Data
@Schema(description = "密码修改请求数据模型")
public class PasswordUpdateDTO {

    /** 用户ID */
    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /** 原密码（校验身份） */
    @NotBlank(message = "原密码不能为空")
    @Schema(description = "用户原密码，用于身份校验", example = "OldPass123")
    private String oldPassword;

    /** 新密码（6-20位，含字母和数字） */
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$", message = "新密码需含字母和数字，长度6-20位")
    @Schema(description = "新密码，需包含字母和数字，长度6-20位", example = "NewPass456")
    private String newPassword;

}
