package com.community_shop.backend.entity;


import com.community_shop.backend.component.statusEnum.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User{
    private Long userID;
    private String password;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String role;
    private String bio;
    private int creditScore;
    private int followerCount;
    private int postCount;
    private LocalDateTime initDate;
    private LocalDateTime activityDate;
    private UserStatusEnum status;
}
