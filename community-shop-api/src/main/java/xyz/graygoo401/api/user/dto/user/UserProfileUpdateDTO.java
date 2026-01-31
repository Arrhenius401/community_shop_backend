package xyz.graygoo401.api.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import xyz.graygoo401.api.user.enums.GenderEnum;

import java.util.List;

/**
 * 用户资料更新请求VO
 */
@Data
@Schema(description = "用户资料更新请求数据模型")
public class UserProfileUpdateDTO {

    /** 昵称（1-10位） */
    @Size(min = 1, max = 30, message = "昵称长度需1-30位")
    @Schema(description = "用户昵称，长度1-30位", example = "张三")
    private String username;

    /** 头像URL（需符合URL格式） */
    @Pattern(regexp = "^https?://.+$", message = "头像URL格式错误")
    @Schema(description = "用户头像URL，需符合HTTP/HTTPS协议格式", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    /** 个性签名（0-50字） */
    @Schema(description = "用户个性签名，长度0-50字", example = "热爱生活，积极向上")
    private String bio;

    /** 兴趣标签列表（不超过200字） */
    @Size(max = 200, message = "个人简介不超过200字")
    @Schema(description = "用户兴趣标签列表", example = "[\"读书\", \"运动\", \"音乐\"]")
    private List<String> interestTags;

    /** 性别（枚举类型） */
    @Schema(description = "用户性别（MALE-男，FEMALE-女，UNKNOWN-未知）", example = "MALE")
    private GenderEnum gender;
}
