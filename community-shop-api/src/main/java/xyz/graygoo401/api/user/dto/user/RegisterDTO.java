package xyz.graygoo401.api.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO（匹配Service层register方法的RegisterVO）
 */
@Data
@Schema(description = "用户注册请求数据模型")
public class RegisterDTO {
    /** 用户名（1-20位字符，非空） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度需1-20位")
    @Schema(description = "注册用户名，长度1-20位", example = "zhangsan123")
    private String username;

    /** 手机号（11位数字，注册/验证码登录场景） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "注册手机号，11位数字", example = "13800138000")
    private String phoneNumber;

    /** 邮箱（标准格式，登录/找回密码场景） */
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式错误")
    @Schema(description = "注册邮箱，符合标准邮箱格式", example = "zhangsan@example.com")
    private String email;

    /** 密码（6-20位，含字母和数字，加密存储） */
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$", message = "密码需含字母和数字，长度6-20位")
    @Schema(description = "注册密码，需包含字母和数字，长度6-20位", example = "Pass123456")
    private String password;

    /** 验证码（6位数字，注册校验） */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
    @Schema(description = "注册验证码，6位数字", example = "123456")
    private String verifyCode;
}
