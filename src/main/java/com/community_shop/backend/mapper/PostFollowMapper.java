package com.community_shop.backend.mapper;

import com.community_shop.backend.enums.CodeEnum.PostFollowStatusEnum;
import com.community_shop.backend.entity.PostFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 跟帖Mapper接口
 * 完全匹配《代码文档1 Mapper层设计.docx》第8节PostFollowMapper的方法定义
 */
@Mapper
public interface PostFollowMapper {

    // 基础 CRUD
    /**
     * 基础新增：发布跟帖（插入跟帖记录）
     * 对应《代码文档1》PostFollowMapper.insert方法
     */
    @Insert("INSERT INTO post_follow (post_id, user_id, content, like_count, create_time, update_time, is_deleted, status) " +
            "VALUES (#{postId}, #{userId}, #{content}, #{likeCount}, #{createTime}, #{updateTime}, #{isDeleted}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "postFollowId") // 返回自增主键
    int insert(PostFollow postFollow);

    /**
     * 基础查询：通过跟帖ID查询详情
     * 对应《代码文档1》PostFollowMapper.selectById方法
     */
    @Select("SELECT post_follow_id, post_id, user_id, content, like_count, create_time, update_time, is_deleted, status " +
            "FROM post_follow " +
            "WHERE post_follow_id = #{postFollowId}")
    PostFollow selectById(Long postFollowId);

    /**
     * 基础更新：更新跟帖内容（仅作者编辑）
     * 对应《代码文档1》PostFollowMapper.updateById方法
     */
    @Update("UPDATE post_follow " +
            "SET content = #{content}, update_time = #{updateTime}, status = #{status} " +
            "WHERE post_follow_id = #{postFollowId} AND user_id = #{userId} AND is_deleted = 0")
    int updateById(PostFollow postFollow);

    /**
     * 基础删除：逻辑删除跟帖（更新is_deleted=1）
     * 对应《代码文档1》PostFollowMapper.deleteById方法
     */
    @Update("UPDATE post_follow " +
            "SET is_deleted = 1, update_time = NOW() " +
            "WHERE post_follow_id = #{postFollowId}")
    int deleteById(Long postFollowId);

    // 列表查询（多维度排序）
    /**
     * 列表查询：按帖子ID查询跟帖列表（分页）
     * 对应《代码文档1》PostFollowMapper.selectByPostId方法
     */
    @Select("SELECT post_follow_id, post_id, user_id, content, like_count, create_time, update_time, is_deleted, status " +
            "FROM post_follow " +
            "WHERE post_id = #{postId} AND is_deleted = 0 AND status = 'NORMAL' " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<PostFollow> selectByPostId(@Param("postId") Long postId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计查询：按帖子ID统计跟帖总数
     * 对应《代码文档1》PostFollowMapper.countByPostId方法
     */
    @Select("SELECT COUNT(*) " +
            "FROM post_follow " +
            "WHERE post_id = #{postId} AND is_deleted = 0 AND status = 'NORMAL'")
    int countByPostId(Long postId);

    /**
     * 互动更新：更新跟帖点赞数
     * 对应《代码文档1》PostFollowMapper.updateLikeCount方法
     */
    @Update("UPDATE post_follow " +
            "SET like_count = #{likeCount}, update_time = NOW() " +
            "WHERE post_follow_id = #{postFollowId} AND is_deleted = 0")
    int updateLikeCount(@Param("postFollowId") Long postFollowId, @Param("likeCount") int likeCount);

    /**
     * 管理更新：更新跟帖状态（管理员操作）
     * 对应《代码文档1》PostFollowMapper.updateStatus方法
     */
    @Update("UPDATE post_follow " +
            "SET status = #{status}, update_time = NOW() " +
            "WHERE post_follow_id = #{postFollowId} AND is_deleted = 0")
    int updateStatus(@Param("postFollowId") Long postFollowId, @Param("status") PostFollowStatusEnum status);
}
