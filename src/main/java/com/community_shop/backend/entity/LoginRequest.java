package com.community_shop.backend.entity;

public class LoginRequest {
    private String email;
    private Long phoneNumber;
    private String password;

    //无参构造
    public LoginRequest() {}

    //全参构造
    public LoginRequest(String email, Long phoneNumber, String password) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public Long getPhoneNumber() {return phoneNumber;}

    public void setPhoneNumber(Long phoneNumber) {this.phoneNumber = phoneNumber;}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
