package xyz.graygoo401.api.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.enums.UserStatusEnum;

import java.time.LocalDateTime;

/**
 * 用户列表项DTO（匹配Service层selectUsersByQuery方法的返回值）
 */
@Data
@Schema(description = "用户列表项响应数据模型")
public class UserListItemDTO {

    /** 用户ID（唯一标识） */
    @Schema(description = "用户唯一标识ID", example = "1001")
    private Long userId;

    /** 用户名（登录显示用） */
    @Schema(description = "用户登录显示名称", example = "zhangsan123")
    private String username;

    /** 邮箱号（登录用） */
    @Schema(description = "用户登录邮箱", example = "zhangsan@example.com")
    private String email;

    /** 手机号（登录用） */
    @Schema(description = "用户登录手机号", example = "13800138000")
    private String phoneNumber;

    /** 头像URL（匹配数据库profile_picture字段） */
    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    /** 信用分（初始100分，匹配credit_score字段） */
    @Schema(description = "用户信用分数，初始100分", example = "100")
    private int creditScore;

    /** 注册时间（数据库创建时间） */
    @Schema(description = "用户注册时间", example = "2023-01-01T12:00:00")
    private LocalDateTime createTime;

    /** 用户状态 */
    @Schema(description = "用户账号状态（NORMAL-正常，DISABLED-禁用）", example = "NORMAL")
    private UserStatusEnum status;

    /** 用户角色 */
    @Schema(description = "用户角色（USER-普通用户，ADMIN-管理员）", example = "USER")
    private UserRoleEnum role;
}
