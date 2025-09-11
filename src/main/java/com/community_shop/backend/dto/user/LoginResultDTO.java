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
    private User user;

    /** 登录令牌（JWT） */
    private String token;

    /** 令牌过期时间 */
    private LocalDateTime tokenExpireTime;

    public LoginResultDTO(){}

}
