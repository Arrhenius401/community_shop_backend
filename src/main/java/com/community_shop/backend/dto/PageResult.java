package com.community_shop.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装，适配《文档》中分页方法的返回数据（如selectPostListByBar、selectProductByKeyword）
 * @param <T> 泛型参数，存储分页数据列表（如Post、Product）
 */
@Data
public class PageResult<T> {
    // 总记录数
    private Long total;
    // 总页数
    private Long totalPages;
    // 当前页数据列表
    private List<T> list;
    // 当前页码
    private Integer pageNum;
    // 每页条数
    private Integer pageSize;

    // @Data 不会提供构造函数
    // 添加无参构造函数
    public PageResult() {}

}
