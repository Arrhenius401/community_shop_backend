package com.community_shop.backend.entity;

public class LocalToken {
    private User user;
    private String subject;
    private String status;
    private String token;

    public LocalToken(){

    }

    public LocalToken(User user, String subject, String status, String token) {
        this.user = user;
        this.subject = subject;
        this.status = status;
        this.token = token;
    }

    public String getSubject()  {return subject; }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
