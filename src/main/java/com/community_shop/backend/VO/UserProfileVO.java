package com.community_shop.backend.VO;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileVO {
    // 昵称（显示名称，可修改）
    private String nickname;

    // 头像URL（阿里云OSS存储路径）
    private String avatarUrl;

    // 个人简介（用户自我描述）
    private String bio;

    // 兴趣标签列表（如"摄影"、"美食"等）
    private List<String> interestTags;

    // 性别（男/女/保密）
    private String gender;

    // 所在城市
    private String city;
}
