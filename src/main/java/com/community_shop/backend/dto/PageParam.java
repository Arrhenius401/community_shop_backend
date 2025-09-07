package com.community_shop.backend.dto;

/**
 * 分页查询通用参数，适配《文档》中所有分页查询方法（如帖子列表、商品搜索）
 */
public class PageParam {
    // 当前页码（默认1）
    private Integer pageNum = 1;
    // 每页条数（默认10）
    private Integer pageSize = 10;

    // Getter & Setter
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}