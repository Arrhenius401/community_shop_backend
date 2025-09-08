package com.community_shop.backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应DTO（匹配Service层selectUserById方法的返回值）
 */
@Data
public class UserDetailDTO {
    /** 用户ID（唯一标识） */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 用户名（登录显示用） */
    private String username;

    /** 头像URL（匹配数据库profile_picture字段） */
    private String avatarUrl;

    /** 信用分（初始100分，匹配credit_score字段） */
    private Integer creditScore;

    /** 发帖数（匹配post_count字段） */
    private Integer postCount;

    /** 兴趣标签列表（将数据库逗号分隔的字符串转为列表） */
    private List<String> interestTags;

    /** 注册时间（数据库创建时间） */
    private LocalDateTime createTime;
}
