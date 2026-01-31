package xyz.graygoo401.community.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.graygoo401.community.dao.entity.UserPostLike;

import java.util.List;

/**
 * 用户帖子点赞关联Mapper接口，提供点赞关系的数据库操作
 * 对应数据库表：user_post_like
 */
@Mapper
public interface UserPostLikeMapper extends BaseMapper<UserPostLike> {


    // ==================== 基础操作 ====================

    /**
     * 取消点赞（逻辑删除）
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteByUserAndPost(
            @Param("userId") Long userId,
            @Param("postId") Long postId
    );

    /**
     * 查询用户是否已点赞该帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 点赞记录（非null表示已点赞）
     */
    UserPostLike selectByUserAndPost(
            @Param("userId") Long userId,
            @Param("postId") Long postId
    );

    /**
     * 判断用户是否已点赞该帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 1=已点赞，0=未点赞
     */
    @Select("SELECT COUNT(1) FROM user_post_like " +
            "WHERE user_id = #{userId} AND post_id = #{postId}")
    Integer selectIsLiked(@Param("userId") Long userId, @Param("postId") Long postId);


    // ==================== 统计与列表查询 ====================
    /**
     * 统计帖子的点赞数
     * @param postId 帖子ID
     * @return 点赞总数
     */
    int countByPostId(@Param("postId") Long postId);

    /**
     * 统计用户点赞的帖子数
     * @param userId 用户ID
     * @return 帖子数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询用户点赞的所有帖子ID
     * @param userId 用户ID
     * @return 帖子ID列表（按点赞时间倒序）
     */
    List<Long> selectPostIdsByUserId(Long userId);

    /**
     * 查询点赞某帖子的用户ID列表
     * @param postId 帖子ID
     * @param limit 限制条数
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByPostId(
            @Param("postId") Long postId,
            @Param("limit") int limit
    );



    // ==================== 批量操作 ====================
    /**
     * 批量删除帖子的所有点赞记录（帖子删除时调用）
     * @param postId 帖子ID
     * @return 影响行数
     */
    int batchDeleteByPostId(@Param("postId") Long postId);

    /**
     * 批量删除用户的所有点赞记录（用户注销时调用）
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchDeleteByUserId(@Param("userId") Long userId);

}
