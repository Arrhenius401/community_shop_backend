package com.community_shop.backend.dto.user;

import com.community_shop.backend.enums.SimpleEnum.GenderEnum;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户资料更新请求VO
 */
@Data
public class UserProfileUpdateDTO {

    /** 昵称（1-10位） */
    @Size(min = 1, max = 10, message = "昵称长度需1-10位")
    private String username;

    /** 头像URL（非空，需符合URL格式） */
    @Pattern(regexp = "^https?://.+$", message = "头像URL格式错误")
    private String avatarUrl;

    // 个人简介（用户自我描述）
    private String bio;

    /** 个人简介（不超过200字） */
    @Size(max = 200, message = "个人简介不超过200字")
    private List<String> interestTags;

    // 性别（男/女/保密）
    private GenderEnum gender;

    // 所在城市
    private String city;
}
