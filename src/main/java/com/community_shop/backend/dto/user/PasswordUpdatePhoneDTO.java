package com.community_shop.backend.dto.user;

import com.community_shop.backend.dto.verification.VerifyPhoneDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机号途径修改密码请求DTO
 */
@Data
@Schema(description = "手机号修改密码请求数据模型")
public class PasswordUpdatePhoneDTO extends VerifyPhoneDTO {

    /** 新密码（6-20位，含字母和数字） */
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$", message = "新密码需含字母和数字，长度6-20位")
    @Schema(description = "新密码", example = "123456")
    private String newPassword;
}
