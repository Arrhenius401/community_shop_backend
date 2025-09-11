package com.community_shop.backend.service.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;

import java.io.Serializable;
import java.util.List;

/**
 * 通用基础服务接口
 * 基于MyBatis-Plus实现，定义核心CRUD操作
 *
 * @param <T> 实体类
 */
public interface BaseService<T> {

    /**
     * 新增数据
     * @param entity 实体对象
     * @return 新增后实体（含自增主键）
     */
    boolean save(T entity);

    /**
     * 根据ID删除实体
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean removeById(Serializable id);

    /**
     * 批量删除实体
     * @param ids 主键ID列表
     * @return 是否删除成功
     */
    boolean removeByIds(List<? extends Serializable> ids);

    /**
     * 根据ID更新实体
     * @param entity 实体对象（含主键）
     * @return 是否更新成功
     */
    boolean updateById(T entity);

    /**
     * 新增或更新实体（存在主键则更新，否则新增）
     * @param entity 实体对象
     * @return 是否操作成功
     */
    boolean saveOrUpdate(T entity);

    /**
     * 根据ID查询实体
     * @param id 主键ID
     * @return 实体对象，无数据则返回null
     */
    T getById(Serializable id);

    /**
     * 查询所有实体
     * @return 实体列表
     */
    List<T> list();

    /**
     * 条件查询实体列表
     * @param queryWrapper 查询条件构造器
     * @return 实体列表
     */
    List<T> list(Wrapper<T> queryWrapper);

    /**
     * 分页查询（使用自定义分页参数和返回结果）
     * @param pageParam 自定义分页参数（含页码、每页条数等）
     * @return 自定义分页结果（含数据列表、总条数等）
     */
    PageResult<T> page(PageParam pageParam);

    /**
     * 带条件的分页查询
     * @param pageParam 自定义分页参数
     * @param queryWrapper MyBatis-Plus条件构造器（内部使用）
     * @return 自定义分页结果
     */
    PageResult<T> page(PageParam pageParam, Wrapper<T> queryWrapper);

}
