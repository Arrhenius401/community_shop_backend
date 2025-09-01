package com.community_shop.backend.entity;

import lombok.Data;

@Data
public class LocalToken {
    private User user;
    private String subject;
    private String status;
    private String token;

    public LocalToken(){}

    public LocalToken(User user, String subject, String status, String token) {
        this.user = user;
        this.subject = subject;
        this.status = status;
        this.token = token;
    }

}
