package xyz.graygoo401.api.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录信息DTO，含本地token
 */
@Data
@Schema(description = "登录结果响应数据模型")
public class LoginResultDTO {
    /** 用户基础信息 */
    @Schema(description = "用户简易信息")
    private UserSimpleDTO userInfo;

    /** 登录令牌（JWT） */
    @Schema(description = "登录成功后返回的JWT令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /** 令牌过期时间 */
    @Schema(description = "令牌过期时间", example = "2023-12-31T23:59:59")
    private LocalDateTime tokenExpireTime;

    /**
     * 用户简易信息内部类（适配登录场景精简返回）
     */
    @Data
    @Schema(description = "用户简易信息")
    public static class UserSimpleDTO {
        /** 用户ID */
        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        /** 用户名 */
        @Schema(description = "用户名", example = "zhangsan123")
        private String username;

        /** 头像URL */
        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;

        /** 用户信用分 */
        @Schema(description = "用户信用分", example = "100")
        private Integer creditScore;

        /** 是否是管理员 */
        @Schema(description = "是否是管理员", example = "false")
        private Boolean isAdmin;
    }
}
