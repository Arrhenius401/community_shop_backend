package com.community_shop.backend.dto.user;

import com.community_shop.backend.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录信息DTO，含本地token
 */
@Data
public class LoginResultDTO {
    /** 用户基础信息 */
    private UserSimpleDTO userInfo;

    /** 登录令牌（JWT） */
    private String token;

    /** 令牌过期时间 */
    private LocalDateTime tokenExpireTime;

    /**
     * 用户简易信息内部类（适配登录场景精简返回）
     */
    @Data
    public static class UserSimpleDTO {
        private Long userId;       // 用户ID（文档4_数据库设计user表主键）
        private String username;   // 用户名
        private String avatarUrl;  // 头像URL（匹配profile_picture字段）
        private Integer creditScore; // 信用分（用于业务权限判断）
    }

}
