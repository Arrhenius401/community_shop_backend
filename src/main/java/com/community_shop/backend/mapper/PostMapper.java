package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.dto.post.PostQueryDTO;
import com.community_shop.backend.entity.Post;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子管理模块Mapper接口，对应post表操作
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    //MyBatis不支持Java的重载特性
    //MyBatis 会把 Mapper 接口里的每个方法映射成一个唯一的 SQL 语句
    //映射的键由命名空间（也就是 Mapper 接口的全限定名）和方法名组合而成

    // ==================== 基础CRUD ====================
    /**
     * 发布帖子
     * @param post 帖子实体（含标题、内容、发布者ID等信息）
     * @return 影响行数
     */
    int insert(Post post);

    /**
     * 通过帖子ID查询帖子详情
     * @param postId 帖子唯一标识
     * @return 帖子完整实体
     */
    Post selectById(@Param("postId") Long postId);

    /**
     * 更新帖子完整信息
     * @param post 帖子实体（含需更新的字段）
     * @return 影响行数
     */
    int updateById(Post post);

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteById(@Param("postId") Long postId);

    // ==================== 列表查询（多维度排序） ====================
    /**
     * 查询指定用户发布的所有帖子
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> getAllPostByUserID(@Param("userId") Long userId);

    /**
     * 查询指定状态的帖子并限制数量
     * @param status 帖子状态
     * @param limit 限制条数
     * @return 帖子列表
     */
    List<Post> getAllPostAddLimit(@Param("status") String status, @Param("limit") int limit);

    /**
     * 分页查询用户发布的帖子
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 帖子分页列表
     */
    List<Post> selectByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 查询热门帖子
     * @param limit 限制条数
     * @return 热门帖子列表
     */
    List<Post> selectHotPosts(@Param("limit") int limit);

    /**
     * 分页查询精华帖子
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 精华帖子分页列表
     */
    List<Post> selectEssencePosts(
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 查询置顶帖子
     * @param limit 限制条数
     * @return 置顶帖子列表
     */
    List<Post> selectTopPosts(@Param("limit") int limit);

    /**
     * 多条件分页查询帖子
     * @param keyword 关键词
     * @param status 帖子状态
     * @param offset 偏移量
     * @param limit 每页条数
     * @param orderBy 排序字段
     * @param direction 排序方向
     * @return 帖子分页列表
     */
    List<Post> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("orderBy") String orderBy,
            @Param("direction") String direction
    );

    /**
     * 根据查询条件统计帖子数量
     * @param queryDTO 查询条件DTO
     * @return 符合条件的帖子总数
     */
    int countByQuery(PostQueryDTO queryDTO);

    /**
     * 根据查询条件分页查询帖子列表
     * @param queryDTO 查询条件DTO（含分页参数、关键词、排序条件等）
     * @return 帖子列表
     */
    List<Post> selectByQuery(PostQueryDTO queryDTO);


    // ==================== 互动数据更新 ====================
    /**
     * 更新帖子点赞数
     * @param postId 帖子ID
     * @param count 调整后的点赞数
     * @return 影响行数
     */
    int updateLikeCount(
            @Param("postId") Long postId,
            @Param("count") int count
    );

    /**
     * 更新帖子评论数
     * @param postId 帖子ID
     * @param count 调整后的评论数
     * @return 影响行数
     */
    int updatePostFollowCount(
            @Param("postId") Long postId,
            @Param("count") int count
    );


    // ==================== 管理功能 ====================
    /**
     * 更新帖子状态
     * @param postID 帖子ID
     * @param status 帖子状态
     */
    void updatePostStatus(@Param("postID") Long postID, @Param("status") String status);

    /**
     * 设置帖子为精华/置顶
     * @param postId 帖子ID
     * @param isEssence 是否精华
     * @param isTop 是否置顶
     * @return 影响行数
     */
    int updatePostStatus(
            @Param("postId") Long postId,
            @Param("isEssence") boolean isEssence,
            @Param("isTop") boolean isTop
    );

    /**
     * 单独更新帖子标题和内容
     * @param postId 帖子ID
     * @param newTitle 新标题
     * @param newContent 新内容
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updatePostContent(
            @Param("postId") Long postId,
            @Param("newTitle") String newTitle,
            @Param("newContent") String newContent,
            @Param("updateTime") LocalDateTime updateTime
    );

    /**
     * 更新帖子精华和置顶状态
     *
     * @param postId 帖子ID
     * @param isEssence 是否精华
     * @param isTop 是否置顶
     * @return 影响行数
     */
    @Update("UPDATE post SET is_essence = #{isEssence}, is_top = #{isTop}, update_time = #{updateTime} WHERE post_id = #{postId}")
    int updateEssenceAndTopById(Long postId, boolean isEssence, boolean isTop, LocalDateTime updateTime);

    /**
     * 批量更新帖子状态
     * @param postIds 帖子ID列表
     * @param status 目标状态
     * @return 影响行数
     */
    int batchUpdateStatus(
            @Param("postIds") List<Long> postIds,
            @Param("status") String status
    );


    // ==================== 统计功能 ====================

    /**
     * 查询指定用户发布帖子的数量
     *
     * @param userId 用户ID
     * @return 帖子数量
     */
    @Select("SELECT COUNT(*) FROM post WHERE user_id = #{userId}")
    int countPostsByUserId(@Param("userId") Long userId);

    /**
     * 获取置顶帖子数量
     *
     * @return 置顶帖子数量
     */
    @Select("SELECT COUNT(*) FROM post WHERE is_top = 1")
    int countTopPosts();

    /**
     * 获取精华帖子数量
     *
     * @return 精华帖子数量
     */
    @Select("SELECT COUNT(*) FROM post WHERE is_essence = 1")
    int countEssencePosts();
}