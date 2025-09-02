package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.param.PageParam;
import com.community_shop.backend.DTO.result.PageResult;
import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.VO.PostVO;
import com.community_shop.backend.entity.Post;

import java.util.List;

public interface PostService {
    // 获取所有帖子
    List<Post> getAllPosts();

    // 获取帖子详情
    Post getPostById(Long id);

    // 添加帖子
    int addPost(Post post);

    // 更新帖子信息
    int updatePost(Post post);

    // 删除帖子
    int deletePost(Long id);

    /**
     * 新增帖子（基础CRUD）
     * 核心逻辑：自动生成创建时间，初始化点赞数/评论数为0，调用PostMapper.insert插入
     * @param post 帖子实体（含title、content、userId、barName，不含post_id）
     * @return ResultDTO<Long> 成功返回新增帖子ID，失败返回错误信息
     * @see com.community_shop.backend.mapper.PostMapper#insert(Post)
     */
    ResultDTO<Long> insertPost(Post post);

    /**
     * 按帖子ID查询（基础CRUD）
     * 核心逻辑：调用PostMapper.selectById查询，关联UserService获取发布者信息
     * @param postId 帖子ID（主键）
     * @return ResultDTO<Post> 成功返回含发布者信息的帖子详情，失败返回错误信息
     * @see com.community_shop.backend.mapper.PostMapper#selectById(Long)
     * @see UserService#selectUserById(Long)
     */
    ResultDTO<Post> selectPostById(Long postId);

    /**
     * 按主题吧查询帖子列表（基础CRUD，分页）
     * 核心逻辑：调用PostMapper.selectByBarName查询，按orderType排序（time=时间倒序，hot=热度倒序）
     * @param barName 主题吧名称（如"数码交流"）
     * @param orderType 排序类型（"time"=最新，"hot"=热门）
     * @param pageParam 分页参数（页码、每页条数）
     * @return ResultDTO<PageResult<Post>> 成功返回分页帖子列表，失败返回错误信息
     * @see com.community_shop.backend.mapper.PostMapper#selectByBarName(String, String, int, int)
     */
    ResultDTO<PageResult<Post>> selectPostListByBar(String barName, String orderType, PageParam pageParam);

    /**
     * 更新帖子内容（基础CRUD）
     * 核心逻辑：校验仅帖子作者可操作，调用PostMapper.updateById更新内容
     * @param postId 帖子ID
     * @param newContent 新帖子内容
     * @param userId 操作用户ID（需与帖子作者ID一致）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息（如"无权限编辑"）
     * @see com.community_shop.backend.mapper.PostMapper#updateById(Post)
     */
    ResultDTO<Boolean> updatePostContent(Long postId, String newContent, Long userId);

    /**
     * 按帖子ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验作者或管理员权限，删除帖子同时同步删除关联点赞记录
     * @param postId 待删除帖子ID
     * @param operatorId 操作用户ID（作者或管理员）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息
     * @see com.community_shop.backend.mapper.PostMapper#deleteById(Long)
     * @see com.community_shop.backend.mapper.UserPostLikeMapper#deleteByPostId(Long)
     */
    ResultDTO<Boolean> deletePostById(Long postId, Long operatorId);

    /**
     * 发布帖子（业务方法）
     * 核心逻辑：校验用户信用分≥60分，新用户帖子标记"待审核"，上传图片至阿里云OSS，调用insertPost
     * @param postVO 帖子发布参数（标题、内容、图片列表、主题吧名称）
     * @param userId 发布者ID
     * @return ResultDTO<String> 成功返回"发布成功"，失败返回错误信息（如"信用分过低"）
     * @see #insertPost(Post)
     * @see UserService#selectUserById(Long)
     * @see com.community_shop.backend.utils.OssUtil （阿里云OSS上传工具）
     */
    ResultDTO<String> publishPost(PostVO postVO, Long userId);

    /**
     * 更新帖子点赞数（业务方法）
     * 核心逻辑：判断用户是否已点赞，点赞则新增关联记录+点赞数+1，取消则删除记录+点赞数-1
     * @param postId 帖子ID
     * @param userId 操作用户ID
     * @param isLike 是否点赞（true=点赞，false=取消点赞）
     * @return ResultDTO<Integer> 成功返回更新后的点赞数，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserPostLikeMapper#selectIsLiked(Long, Long)
     * @see com.community_shop.backend.mapper.PostMapper#updateLikeCount(Long, int)
     */
    ResultDTO<Integer> updatePostLikeCount(Long postId, Long userId, Boolean isLike);

    /**
     * 帖子加精/置顶（业务方法）
     * 核心逻辑：校验管理员权限，置顶数≤5篇，调用PostMapper.updatePostStatus更新状态
     * @param postId 帖子ID
     * @param isEssence 是否加精（true=加精，false=取消加精）
     * @param isTop 是否置顶（true=置顶，false=取消置顶）
     * @param adminId 管理员ID（需通过UserService校验角色）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息（如"置顶数量超限"）
     * @see com.community_shop.backend.mapper.PostMapper#updatePostStatus(Long, boolean, boolean)
     * @see UserService#selectUserById(Long)
     */
    ResultDTO<Boolean> setPostEssenceOrTop(Long postId, Boolean isEssence, Boolean isTop, Long adminId);
}
