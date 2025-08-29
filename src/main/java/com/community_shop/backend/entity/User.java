package com.community_shop.backend.entity;


import com.community_shop.backend.component.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private String gender;
    private int creditScore;
    private int followerCount;
    private int postCount;
    private LocalDateTime initDate;
    private LocalDateTime activityDate;
    private UserStatusEnum status;
    private List<String> interestTags;

}
