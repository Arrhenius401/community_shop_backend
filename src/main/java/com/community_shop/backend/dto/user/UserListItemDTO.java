package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表项DTO（匹配Service层selectUsersByQuery方法的返回值）
 */
@Data
public class UserListItemDTO {

    /** 用户ID（唯一标识） */
    private Long userId;

    /** 用户名（登录显示用） */
    private String username;

    /** 邮箱号（登录用） */
    private String email;

    /** 手机号（登录用） */
    private String phoneNumber;

    /** 头像URL（匹配数据库profile_picture字段） */
    private String avatarUrl;

    /** 信用分（初始100分，匹配credit_score字段） */
    private int creditScore;

    /** 注册时间（数据库创建时间） */
    private LocalDateTime createTime;

    /** 用户状态 */
    private UserStatusEnum status;

    /** 用户角色 */
    private UserRoleEnum role;
}
