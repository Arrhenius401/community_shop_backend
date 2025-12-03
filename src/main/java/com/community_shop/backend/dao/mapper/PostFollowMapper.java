package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.dto.post.PostFollowQueryDTO;
import com.community_shop.backend.dto.post.PostQueryDTO;
import com.community_shop.backend.entity.Post;
import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import com.community_shop.backend.entity.PostFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 帖子评论模块Mapper接口，对应post_follow表操作
 */
@Mapper
public interface PostFollowMapper extends BaseMapper<PostFollow> {

    // ==================== 关联查询 ====================
    /**
     * 查询指定帖子的所有跟帖（分页）
     * @param postId 帖子ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 评论分页列表
     */
    List<PostFollow> selectByPostId(
            @Param("postId") Long postId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 查询指定用户发布的所有跟帖
     * @param userId 用户ID
     * @return 评论列表
     */
    List<PostFollow> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定跟帖的回复列表
     * @param parentId 父跟帖ID
     * @return 回复列表
     */
    List<PostFollow> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据查询条件统计帖子数量
     * @param queryDTO 查询条件DTO
     * @return 符合条件的帖子总数
     */
    int countByQuery(@Param("query") PostFollowQueryDTO queryDTO);

    /**
     * 根据查询条件分页查询帖子列表
     * @param queryDTO 查询条件DTO（含分页参数、关键词、排序条件等）
     * @return 帖子列表
     */
    List<PostFollow> selectByQuery(@Param("query") PostFollowQueryDTO queryDTO);


    // ==================== 统计功能 ====================
    /**
     * 统计指定帖子的跟帖总数
     * @param postId 帖子ID
     * @return 跟帖总数
     */
    int countByPostId(@Param("postId") Long postId);

    /**
     * 统计指定用户的跟帖总数
     * @param userId 用户ID
     * @return 评跟帖数
     */
    int countByUserId(@Param("userId") Long userId);


    // ==================== 批量操作 ====================
    /**
     * 批量删除指定帖子的所有评论（帖子删除时联动）
     * @param postId 帖子ID
     * @return 影响行数
     */
    int batchDeleteByPostId(@Param("postId") Long postId);

    /**
     * 批量删除指定用户的所有评论（用户注销时联动）
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchDeleteByUserId(@Param("userId") Long userId);


    // ==================== 管理功能 ====================
    /**
     * 管理更新：更新跟帖状态（管理员操作）
     * 对应《代码文档1》PostFollowMapper.updateStatus方法
     */
    @Update("UPDATE post_follow " +
            "SET status = #{status}, update_time = NOW() " +
            "WHERE post_follow_id = #{postFollowId} AND is_deleted = 0")
    int updateStatus(@Param("postFollowId") Long postFollowId, @Param("status") PostFollowStatusEnum status);
}
