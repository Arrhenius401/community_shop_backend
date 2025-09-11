package com.community_shop.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.service.base.BaseService;

import java.io.Serializable;
import java.util.List;

/**
 * BaseService接口的通用实现类
 * 封装基础CRUD操作，通过泛型适配不同实体类
 * 基于MyBatis-Plus的ServiceImpl，实现BaseService接口
 *
 * @param <M> Mapper接口类型，需继承MyBatis-Plus的BaseMapper
 * @param <T> 实体类类型
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements BaseService<T> {

    @Override
    public boolean save(T entity) {
        return super.save(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    public boolean removeByIds(List<? extends Serializable> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean updateById(T entity) {
        return super.updateById(entity);
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    public T getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public List<T> list() {
        return super.list();
    }

    @Override
    public List<T> list(Wrapper<T> queryWrapper) {
        return super.list(queryWrapper);
    }

    /**
     * 分页查询：将自定义PageParam转换为IPage，结果转换为PageResult
     */
    @Override
    public PageResult<T> page(PageParam pageParam) {
        return page(pageParam, null);
    }

    @Override
    public PageResult<T> page(PageParam pageParam, Wrapper<T> queryWrapper) {
        // 1. 将自定义PageParam转换为MyBatis-Plus的IPage
        Page<T> mpPage = new Page<>(
                pageParam.getPageNum(),  // 页码（从1开始）
                pageParam.getPageSize()  // 每页条数
        );

        // 2. 调用MyBatis-Plus的分页查询
        IPage<T> resultPage = super.page(mpPage, queryWrapper);

        // 3. 将IPage转换为项目自定义的PageResult
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(resultPage.getRecords());       // 数据列表
        pageResult.setTotal(resultPage.getTotal());         // 总条数
        pageResult.setPageNum(pageParam.getPageNum());      // 当前页码
        pageResult.setPageSize(pageParam.getPageSize());    // 每页条数
        pageResult.setTotalPages((long) resultPage.getPages()); // 总页数

        return pageResult;
    }

}
