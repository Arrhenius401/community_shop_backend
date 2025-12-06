package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.enums.code.UserStatusEnum;
import com.community_shop.backend.enums.simple.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应DTO（匹配Service层selectUserById方法的返回值）
 */
@Data
@Schema(description = "用户详情响应数据模型")
public class UserDetailDTO {
    /** 用户ID（唯一标识） */
    @NotNull(message = "用户ID不能为空")
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

    /** 个性签名（用户自我描述） */
    @Schema(description = "用户个性签名", example = "热爱生活，积极向上")
    private String bio;

    /** 信用分（初始100分，匹配credit_score字段） */
    @Schema(description = "用户信用分数", example = "100")
    private Integer creditScore;

    /** 发帖数（匹配post_count字段） */
    @Schema(description = "用户发帖数量", example = "20")
    private Integer postCount;

    /** 兴趣标签列表（将数据库逗号分隔的字符串转为列表） */
    @Schema(description = "用户兴趣标签列表", example = "[\"读书\", \"运动\", \"音乐\"]")
    private List<String> interestTags;

    /** 注册时间（数据库创建时间） */
    @Schema(description = "用户注册时间", example = "2023-01-01T12:00:00")
    private LocalDateTime createTime;

    /** 最后活跃时间（数据库更新时间） */
    @Schema(description = "用户最后活跃时间", example = "2023-10-01T15:30:00")
    private LocalDateTime activityTime;

    /** 性别（枚举类型，匹配数据库gender字段） */
    @Schema(description = "用户性别（MALE-男，FEMALE-女，UNKNOWN-未知）", example = "MALE")
    private GenderEnum gender;

    /** 用户状态 */
    @Schema(description = "用户账号状态（NORMAL-正常，DISABLED-禁用）", example = "NORMAL")
    private UserStatusEnum status;

    /** 用户角色 */
    @Schema(description = "用户角色（USER-普通用户，ADMIN-管理员）", example = "USER")
    private UserRoleEnum role;
}