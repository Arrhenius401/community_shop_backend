package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.UserPostLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户帖子点赞关联Mapper接口，提供点赞关系的数据库操作
 * 对应数据库表：user_post_like
 */
@Mapper
public interface UserPostLikeMapper {
    /**
     * 新增点赞记录
     * @param userPostLike 点赞实体（含userId、postId、likeTime）
     * @return 插入影响行数（1=成功，0=失败）
     */
    @Insert("INSERT INTO user_post_like (user_id, post_id, like_time) " +
            "VALUES (#{userId}, #{postId}, #{likeTime})")
    int insert(UserPostLike userPostLike);

    /**
     * 取消点赞（按用户和帖子删除）
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 删除影响行数（1=成功，0=无匹配记录）
     */
    @Delete("DELETE FROM user_post_like " +
            "WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 判断用户是否已点赞该帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 1=已点赞，0=未点赞
     */
    @Select("SELECT COUNT(1) FROM user_post_like " +
            "WHERE user_id = #{userId} AND post_id = #{postId}")
    Integer selectIsLiked(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 统计帖子的总点赞数
     * @param postId 帖子ID
     * @return 点赞总数
     */
    @Select("SELECT COUNT(1) FROM user_post_like " +
            "WHERE post_id = #{postId}")
    Integer selectLikeCountByPostId(Long postId);

    /**
     * 查询用户点赞的所有帖子ID
     * @param userId 用户ID
     * @return 帖子ID列表（按点赞时间倒序）
     */
    @Select("SELECT post_id FROM user_post_like " +
            "WHERE user_id = #{userId} " +
            "ORDER BY like_time DESC")
    List<Long> selectPostIdsByUserId(Long userId);

    /**
     * 查询帖子的所有点赞用户ID
     * @param postId 帖子ID
     * @return 用户ID列表（按点赞时间倒序）
     */
    @Select("SELECT user_id FROM user_post_like " +
            "WHERE post_id = #{postId} " +
            "ORDER BY like_time DESC")
    List<Long> selectUserIdsByPostId(Long postId);

    /**
     * 批量删除某帖子的所有点赞记录（帖子删除时调用）
     * @param postId 帖子ID
     * @return 删除影响行数
     */
    @Delete("DELETE FROM user_post_like " +
            "WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);
}
