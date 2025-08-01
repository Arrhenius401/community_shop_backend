package com.community_shop.backend.entity;

public class SelectRequest {
    private String subject;
    private String order;
    private String limit;
    private String offset;
    private String status;

    public SelectRequest(){

    }

    public SelectRequest(String subject, String order, String limit, String offset, String status) {
        this.subject = subject;
        this.order = order;
        this.limit = limit;
        this.offset = offset;
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
