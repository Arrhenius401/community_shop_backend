package com.community_shop.backend.entity;


import com.community_shop.backend.component.enums.UserRoleEnum;
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
    private String bio;
    private String gender;
    private int creditScore;
    private int followerCount;
    private int postCount;
    private LocalDateTime initDate;
    private LocalDateTime activityDate;
    private UserStatusEnum status;
    private UserRoleEnum role;
    private List<String> interestTags;


    public User(Long userID, String password, String username, String email, String phoneNumber, String profilePicture, String bio, String gender, int creditScore, int followerCount, int postCount, LocalDateTime initDate, LocalDateTime activityDate, UserStatusEnum status, UserRoleEnum role, List<String> interestTags) {
        this.userID = userID;
        this.password = password;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.gender = gender;
        this.creditScore = creditScore;
        this.followerCount = followerCount;
        this.postCount = postCount;
        this.initDate = initDate;
        this.activityDate = activityDate;
        this.status = status;
        this.role = role;
        this.interestTags = interestTags;
    }
}
