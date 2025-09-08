package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Post;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostMapper {

    //MyBatis不支持Java的重载特性
    //MyBatis 会把 Mapper 接口里的每个方法映射成一个唯一的 SQL 语句
    //映射的键由命名空间（也就是 Mapper 接口的全限定名）和方法名组合而成

    // 基础CRUD
    /**
     * 发布帖子
     *
     * @param post 帖子实体
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO post(title, content, user_id, like_count, post_follow_count, create_time, status, is_hot) " +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{postFollowCount}, #{createTime}, #{status}, #{ishot})")
    int insert(Post post);

    /**
     * 查询帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子实体
     */
    @Select("SELECT * FROM post WHERE post_id = #{postId}")
    Post selectById(Long postId);

    /**
     * 更新帖子内容
     *
     * @param post 帖子实体
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET title = #{title}, content = #{content}, user_id = #{userID}, like_count = #{likeCount}, " +
            "post_follow_count = #{postFollowCount}, create_time = #{createTime}, status = #{status}, is_hot = #{ishot} " +
            "WHERE post_id = #{postID}")
    int updateById(Post post);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @return 删除结果影响行数
     */
    @Delete("DELETE FROM post WHERE post_id = #{id}")
    int deleteById(Long postId);

    // 列表查询（多维度排序）
    /**
     * 获得指定用户发布的所有帖子
     *
     * @param userID 用户ID
     * @return 帖子列表
     */
    @Select("SELECT * FROM post where user_id = #{userID};")
    List<Post> getAllPostByUserID(Long userID);

    /**
     * 获得所有帖子(不限状态)
     *
     * @return 帖子列表
     */
    @Select("SELECT * FROM post;")
    List<Post> getAllPost();

    /**
     * 获得指定状态的帖子列表(限定指标，数目和偏移量)
     *
     * @param status 帖子状态
     * @param limit 限制数量
     * @return 帖子列表
     */
    @Select("SELECT * FROM post WHERE status = #{status}\n" +
            "ORDER BY post_id DESC\n" +
            "LIMIT #{limit};")
    List<Post> getAllPostAddLimit(String status, int limit);

    /**
     * 插入帖子实体
     *
     * @param post 帖子实体对象
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO post (title, content, user_id, like_count, post_follow_count, create_time, status, is_hot) \n" +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{postFollowCount}, #{createTime}, #{status}, #{ishot});")
    int addPost(Post post);

    // 局部查询

    /**
     * 获得帖子点赞数
     * @param postId
     * @return
     */
    @Select("SELECT like_count FROM post WHERE post_id = #{postId}")
    int selectLikeCountById(Long postId);

    // 局部更新
    /**
     * 更新帖子状态
     *
     * @param postID 帖子ID
     * @param status 新状态值
     */
    @Update("UPDATE post SET status = #{status} where post_id = #{postID}")
    void updatePostStatus(Long postID, String status);

    /**
     * 更新帖子实体
     *
     * @param post 帖子实体对象
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET title = #{title}, content = #{content}, user_id = #{userID}, like_count = #{likeCount}, post_follow_count = #{postFollowCount}, create_time = #{createTime}, status = #{status}, is_hot = #{ishot} where post_id = #{postID}")
    int updatePost(Post post);

    /**
     * 更新点赞数
     *
     * @param postId 帖子ID
     * @param count 点赞数
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET like_count = #{count} WHERE post_id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 更新跟帖数
     *
     * @param postId 帖子ID
     * @param count 评论数
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET post_follow_count = #{count} WHERE post_id = #{postId}")
    int updatePostFollowCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 设置帖子为精华/置顶
     *
     * @param postId 帖子ID
     * @param isEssence 是否精华
     * @param isTop 是否置顶
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET is_essence = #{isEssence}, is_top = #{isTop} WHERE post_id = #{postId}")
    int updatePostStatus(@Param("postId") Long postId,
                         @Param("isEssence") boolean isEssence,
                         @Param("isTop") boolean isTop);


    /**
     * 更新帖子内容
     *
     * @param postId 帖子ID
     * @param newTitle 新标题
     * @param newContent 新内容
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET title = #{newTitle}, content = #{newContent}, update_time = #{updateTime} WHERE post_id = #{postId}")
    int updatePostContent(@Param("postId") Long postId, @Param("newTitle") String newTitle, @Param("newContent") String newContent, @Param("updateTime") LocalDateTime updateTime);

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
     * 删除指定帖子
     *
     * @param id 帖子ID
     * @return 删除结果影响行数
     */
    @Delete("DELETE FROM post WHERE post_id = #{id}")
    int deletePost(Long id);

    /**
     * 查询用户发布的所有帖子
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 帖子列表
     */
    @Select("SELECT * FROM post WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Post> selectByUserId(@Param("userId") Long userId,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    /**
     * 查询热门帖子
     *
     * @param limit 限制数量
     * @return 热门帖子列表
     */
    @Select("SELECT * FROM post WHERE is_hot = 1 ORDER BY like_count DESC, post_follow_count DESC LIMIT #{limit}")
    List<Post> selectHotPosts(@Param("limit") int limit);

    /**
     * 获取精华帖子列表
     *
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 精华帖子列表
     */
    @Select("SELECT * FROM post WHERE is_essence = 1 ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Post> selectEssencePosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取置顶帖子列表
     *
     * @return 置顶帖子列表
     */
    @Select("SELECT * FROM post WHERE is_top = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<Post> selectTopPosts(@Param("limit") int limit);

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