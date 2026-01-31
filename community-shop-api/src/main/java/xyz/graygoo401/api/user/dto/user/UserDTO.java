package xyz.graygoo401.api.user.dto.user;

import lombok.Data;
import xyz.graygoo401.api.user.enums.GenderEnum;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.enums.UserStatusEnum;

import java.time.LocalDateTime;

/**
 * 用户DTO
 */
@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private GenderEnum gender;
    private Integer creditScore;
    private UserRoleEnum role;
    private UserStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 业务逻辑快捷判断
    public boolean isAdmin() { return UserRoleEnum.ADMIN.equals(this.role); }
}