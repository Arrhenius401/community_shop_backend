package com.community_shop.backend.entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User{
    private Long userID;
    private String password;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDateTime initDate;
    private String profilePicture;
    private String role;
    private String status;
    private String bio;
    private int creditScore;
    private int followerCount;
    private int postCount;
}
