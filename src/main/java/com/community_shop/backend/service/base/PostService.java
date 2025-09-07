package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.vo.post.PostDetailVO;
import com.community_shop.backend.vo.post.PostUpdateVO;
import com.community_shop.backend.entity.Post;

import java.util.List;

/**
 * 商品管理Service接口，实现《文档》中商品发布、搜索、库存管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：商品发布（多图/视频）、搜索筛选、库存管理
 * 2. 《文档4_数据库工作（新）.docx》：product表结构（product_id、price、stock、seller_id等）
 * 3. 《代码文档1 Mapper层设计.docx》：ProductMapper的CRUD及库存更新方法
 */
public interface PostService {


    /**
     * 新增帖子（基础CRUD）
     * 核心逻辑：自动生成创建时间，初始化点赞数/评论数为0，调用PostMapper.insert插入
     * @param post 帖子实体（含title、content、userId、barName，不含post_id）
     * @return 新增帖子ID
     * @see com.community_shop.backend.mapper.PostMapper#insert(Post)
     */
    Long insertPost(Post post);

    /**
     * 按帖子ID查询（基础CRUD）
     * 核心逻辑：调用PostMapper.selectById查询，关联UserService获取发布者信息
     * @param postId 帖子ID（主键）
     * @return 含发布者信息的帖子详情
     * @see com.community_shop.backend.mapper.PostMapper#selectById(Long)
     * @see UserService#selectUserById(Long)
     */
    Post selectPostById(Long postId);

    /**
     * 更新帖子内容（基础CRUD）
     * 核心逻辑：校验仅帖子作者可操作，调用PostMapper.updateById更新内容
     * @param postVO 帖子更新参数（标题、内容、图片列表）
     * @param userId 操作用户ID（需与帖子作者ID一致）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.PostMapper#updateById(Post)
     */
    Boolean updatePostContent(PostUpdateVO postVO, Long userId);

    /**
     * 按帖子ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验作者或管理员权限，删除帖子同时同步删除关联点赞记录
     * @param postId 待删除帖子ID
     * @param operatorId 操作用户ID（作者或管理员）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.PostMapper#deleteById(Long)
     * @see com.community_shop.backend.mapper.UserPostLikeMapper#deleteByPostId(Long)
     */
    Boolean deletePostById(Long postId, Long operatorId);

    /**
     * 发布帖子（业务方法）
     * 核心逻辑：校验用户信用分≥60分，新用户帖子标记"待审核"，上传图片至阿里云OSS，调用insertPost
     * @param postVO 帖子发布参数（标题、内容、图片列表、主题吧名称）
     * @param userId 发布者ID
     * @return "发布成功" 或抛出异常
     * @see #insertPost(Post)
     * @see UserService#selectUserById(Long)
     * @see com.community_shop.backend.utils.OssUtil （阿里云OSS上传工具）
     */
    String publishPost(PostDetailVO postVO, Long userId);

    /**
     * 更新帖子点赞数（业务方法）
     * 核心逻辑：判断用户是否已点赞，点赞则新增关联记录+点赞数+1，取消则删除记录+点赞数-1
     * @param postId 帖子ID
     * @param userId 操作用户ID
     * @param isLike 是否点赞（true=点赞，false=取消点赞）
     * @return 更新后的点赞数
     * @see com.community_shop.backend.mapper.UserPostLikeMapper#selectIsLiked(Long, Long)
     * @see com.community_shop.backend.mapper.PostMapper#updateLikeCount(Long, int)
     */
    Integer updatePostLikeCount(Long postId, Long userId, Boolean isLike);

    /**
     * 帖子加精/置顶（业务方法）
     * 核心逻辑：校验管理员权限，置顶数≤5篇，调用PostMapper.updatePostStatus更新状态
     * @param postId 帖子ID
     * @param isEssence 是否加精（true=加精，false=取消加精）
     * @param isTop 是否置顶（true=置顶，false=取消置顶）
     * @param adminId 管理员ID（需通过UserService校验角色）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.PostMapper#updatePostStatus(Long, boolean, boolean)
     * @see UserService#selectUserById(Long)
     */
    Boolean setPostEssenceOrTop(Long postId, Boolean isEssence, Boolean isTop, Long adminId);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectHotPosts获取最热帖子列表
     * @param limit 获取数量
     * @return 最热帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectHotPosts(int)
     */
    List<PostDetailVO> selectHotPosts(Integer limit);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectTopPosts获取置顶帖子列表
     * @return 置顶帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectEssencePosts(int, int)
     */
    PageResult<PostDetailVO> selectEssencePosts(PageParam pageParam);

    /**
     * 获取帖子列表（业务方法）
     * 核心逻辑：调用PostMapper.selectTopPosts获取置顶帖子列表
     * @return 置顶帖子列表
     * @see com.community_shop.backend.mapper.PostMapper#selectTopPosts(int)
     */
    List<PostDetailVO> selectTopPosts();

    /**
     * 辅助方法：将Post实体+关联User信息转换为PostDetailVO
     */
    PostDetailVO convertToDetailVO(Post post);
}
