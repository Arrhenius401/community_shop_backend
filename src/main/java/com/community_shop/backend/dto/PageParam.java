package com.community_shop.backend.dto;

import lombok.Data;

/**
 * 分页查询通用参数，适配《文档》中所有分页查询方法（如帖子列表、商品搜索）
 */
@Data
public class PageParam {
    // 当前页码（默认1）
    private Integer pageNum = 1;
    // 每页条数（默认10）
    private Integer pageSize = 10;

}