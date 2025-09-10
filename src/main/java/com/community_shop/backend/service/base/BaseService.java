package com.community_shop.backend.service.base;

import java.util.List;

/**
 * 通用基础服务接口
 * @param <T> 实体类
 * @param <ID> 主键ID类型
 */
public interface BaseService<T,  ID> {

    /**
     * 新增数据
     * @param entity 实体对象
     * @return 新增后实体（含自增主键）
     */
    T save(T entity);

    /**
     * 批量新增
     * @param entities 实体列表
     * @return 新增成功数量
     */
    int batchSave(List<T> entities);

    /**
     * 按ID查询
     * @param id 主键ID
     * @return 实体对象（无数据返回null）
     */
    T getById(ID id);

    /**
     * 按ID更新（全量更新）
     * @param entity 实体对象（需含主键）
     * @return 更新是否成功
     */
    boolean updateById(T entity);

    /**
     * 按ID逻辑删除（更新status字段）
     * @param id 主键ID
     * @return 删除是否成功
     */
    boolean removeById(ID id);
}
