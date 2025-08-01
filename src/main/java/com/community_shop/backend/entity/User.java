package com.community_shop.backend.entity;


import java.time.LocalDateTime;

public class User{
    private Long userID;
    private String password;
    private String username;
    private String email;
    private Long phoneNumber;
    private LocalDateTime initDate;
    private String profilePicture;
    private String role;
    private String status;
    private String bio;
    private int creditScore;
    private int followerCount;
    private int postCount;

    public User() {
    }

    public User(Long userID, String password, String username, String email, Long phoneNumber,
                LocalDateTime initDate, String profilePicture, String role, String status,
                String bio, int creditScore, int followerCount, int postCount) {
        this.userID = userID;
        this.password = password;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.initDate = initDate;
        this.profilePicture = profilePicture;
        this.role = role;
        this.status = status;
        this.bio = bio;
        this.creditScore = creditScore;
        this.followerCount = followerCount;
        this.postCount = postCount;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getInitDate() {
        return initDate;
    }

    public void setInitDate(LocalDateTime initDate) {
        this.initDate = initDate;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}
