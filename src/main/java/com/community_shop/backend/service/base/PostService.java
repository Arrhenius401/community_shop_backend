package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.post.*;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.exception.BusinessException;

import java.util.List;

/**
 * 商品管理Service接口，实现《文档》中商品发布、搜索、库存管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：商品发布（多图/视频）、搜索筛选、库存管理
 * 2. 《文档4_数据库工作（新）.docx》：product表结构（product_id、price、stock、seller_id等）
 * 3. 《代码文档1 Mapper层设计.docx》：ProductMapper的CRUD及库存更新方法
 */
public interface PostService extends BaseService<Post> {

    /**
     * 发布帖子
     * @param userId 发布者ID
     * @param postPublishDTO 帖子发布参数（标题、内容、图片等）
     * @return 发布成功的帖子详情
     * @throws BusinessException 信用分不足（<60分）、内容违规时抛出
     */
    PostDetailDTO publishPost(Long userId, PostPublishDTO postPublishDTO);

    /**
     * 编辑帖子
     * @param postId 帖子ID
     * @param userId 操作用户ID
     * @param postUpdateDTO 帖子更新参数（标题、内容）
     * @return 编辑后的帖子详情
     * @throws BusinessException 无权限（非作者）、帖子已删除时抛出
     */
    PostDetailDTO updatePost(Long postId, Long userId, PostUpdateDTO postUpdateDTO);

    /**
     * 帖子点赞/取消点赞
     * @param postLikeDTO 点赞参数（帖子ID、用户ID、操作类型）
     * @return 操作后的点赞数
     * @throws BusinessException 帖子不存在、每日点赞次数超限时抛出
     */
    Integer updateLikeStatus(PostLikeDTO postLikeDTO);

    /**
     * 管理员设置帖子精华/置顶
     *
     * @param userId 操作用户ID
     * @param postEssenceTopDTO 状态设置参数（帖子ID、管理员ID、状态）
     * @return 设置是否成功
     * @throws BusinessException 无管理员权限、置顶数超5篇时抛出
     */
    Boolean setEssenceOrTop(Long userId, PostEssenceTopDTO postEssenceTopDTO);

    /**
     * 多条件查询帖子列表
     * @param postQueryDTO 查询参数（关键词、排序、分页）
     * @return 分页帖子列表（轻量展示）
     */
    PageResult<PostListItemDTO> queryPosts(PostQueryDTO postQueryDTO);

    /**
     * 批量删除违规帖子（管理员操作）
     * @param postIds 帖子ID列表
     * @param adminId 管理员ID
     * @return 删除成功数量
     * @throws BusinessException 无管理员权限时抛出
     */
    int batchDeletePosts(Long adminId, List<Long> postIds);

    //========================== v1 ===================================


    /**
     * 按帖子ID查询（基础CRUD）
     * 核心逻辑：调用PostMapper.selectById查询，关联UserService获取发布者信息
     * @param userId 操作用户ID
     * @param postId 帖子ID（主键）
     * @return 含发布者信息的帖子详情
     * @see com.community_shop.backend.mapper.PostMapper#selectById(Long)
     * @see UserService#selectUserById(Long)
     */
    PostDetailDTO selectPostById(Long userId, Long postId);

    /**
     * 更新帖子内容（基础CRUD）
     * 核心逻辑：校验仅帖子作者可操作，调用PostMapper.updateById更新内容
     * @param userId 操作用户ID（需与帖子作者ID一致）
     * @param postUpdateDTO 帖子更新参数（标题、内容）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.PostMapper#updateById(Post)
     */
    Boolean updatePostContent(Long userId, PostUpdateDTO postUpdateDTO);

    /**
     * 按帖子ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验作者或管理员权限，删除帖子同时同步删除关联点赞记录
     * @param operatorId 操作用户ID（作者或管理员）
     * @param postId 待删除帖子ID
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.PostMapper#deleteById(Long)
     * @see com.community_shop.backend.mapper.UserPostLikeMapper#batchDeleteByPostId(Long)
     */
    Boolean deletePostById(Long operatorId, Long postId);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectHotPosts获取最热帖子列表
     * @param limit 获取数量
     * @return 最热帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectHotPosts(int)
     */
    List<PostDetailDTO> selectHotPosts(Integer limit);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectTopPosts获取置顶帖子列表
     * @return 置顶帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectEssencePosts(int, int)
     */
    PageResult<PostDetailDTO> selectEssencePosts(PageParam pageParam);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectTopPosts获取置顶帖子列表
     * @return 置顶帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectTopPosts(int)
     */
    List<PostDetailDTO> selectTopPosts();

}
