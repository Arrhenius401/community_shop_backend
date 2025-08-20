package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    //MyBatis不支持Java的重载特性
    //MyBatis 会把 Mapper 接口里的每个方法映射成一个唯一的 SQL 语句
    //映射的键由命名空间（也就是 Mapper 接口的全限定名）和方法名组合而成

    // SELECT语句
    // 不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM post")
    int getCount();

    // 根据帖子ID获取帖子实例
    @Select("SELECT * FROM post where post_id = #{id};")
    Post getPostByID(Long id);

    // 获得指定用户发布的所有帖子
    @Select("SELECT * FROM post where user_id = #{userID};")
    List<Post> getAllPostByUserID(Long userID);

    // 获得所有帖子(不限状态)
    @Select("SELECT * FROM post;")
    List<Post> getAllPost();

    // 获得所有帖子(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM post WHERE status = #{status}\n" +
            "ORDER BY post_id DESC\n" +
            "LIMIT #{limit};")
    List<Post> getAllPostAddLimit(String status, int limit);

    // 用于直接替换SQL片段（如列名、表名、排序方向等）
    @Select("SELECT * FROM post WHERE ${compareIndex} = #{compareParam}\n" +
            "ORDER BY ${order} ${direction}\n" +
            "LIMIT #{offset}, #{limit};")
    List<Post> getPostsByAllParam(String compareIndex, String compareParam, String order,String direction, int limit, int offset);

    // INSERT语句
    // 插入帖子实体
    @Insert("INSERT INTO post (title, content, user_id, like_count, comment_count, create_time, status, is_hot) \n" +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{commentCount}, #{createTime}, #{status}, #{ishot});")
    int addPost(Post post);

    // UPDATE语句
    // 更新帖子状态
    @Update("UPDATE post SET status = #{status} where post_id = #{postID}")
    void updatePostStatus(Long postID, String status);

    // 更新帖子实体
    @Update("UPDATE post SET title = #{title}, content = #{content}, user_id = #{userID}, like_count = #{likeCount}, comment_count = #{commentCount}, create_time = #{createTime}, status = #{status}, is_hot = #{ishot} where post_id = #{postID}")
    int updatePost(Post post);

    // DELETE语句
    // 删除指定帖子
    @Delete("DELETE FROM post WHERE post_id = #{id}")
    int deletePost(Long id);

    // 新增方法

    /**
     * 发布帖子
     * @param post 帖子实体
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO post(title, content, user_id, like_count, comment_count, create_time, status, is_hot) " +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{commentCount}, #{createTime}, #{status}, #{ishot})")
    int insert(Post post);

    /**
     * 查询帖子详情
     * @param postId 帖子ID
     * @return 帖子实体
     */
    @Select("SELECT * FROM post WHERE post_id = #{postId}")
    Post selectById(Long postId);

    /**
     * 更新帖子内容
     * @param post 帖子实体
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET title = #{title}, content = #{content}, user_id = #{userID}, like_count = #{likeCount}, " +
            "comment_count = #{commentCount}, create_time = #{createTime}, status = #{status}, is_hot = #{ishot} " +
            "WHERE post_id = #{postID}")
    int updateById(Post post);

    /**
     * 按主题吧查询帖子，支持按时间/热度排序
     * @param barName 主题吧名称
     * @param orderType 排序类型(time:按时间排序, hot:按热度排序)
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 帖子列表
     */
    @Select("<script>" +
            "SELECT * FROM post " +
            "WHERE bar_name = #{barName} " +
            "<choose>" +
            "  <when test='orderType == \"hot\"'>ORDER BY like_count DESC, comment_count DESC</when>" +
            "  <otherwise>ORDER BY create_time DESC</otherwise>" +
            "</choose>" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Post> selectByBarName(@Param("barName") String barName,
                               @Param("orderType") String orderType,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /**
     * 查询用户发布的所有帖子
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
     * @param limit 限制数量
     * @return 热门帖子列表
     */
    @Select("SELECT * FROM post WHERE is_hot = 1 ORDER BY like_count DESC, comment_count DESC LIMIT #{limit}")
    List<Post> selectHotPosts(@Param("limit") int limit);

    /**
     * 更新点赞数
     * @param postId 帖子ID
     * @param count 点赞数
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET like_count = #{count} WHERE post_id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 更新评论数
     * @param postId 帖子ID
     * @param count 评论数
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET comment_count = #{count} WHERE post_id = #{postId}")
    int updateCommentCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 设置帖子为精华/置顶
     * @param postId 帖子ID
     * @param isEssence 是否精华
     * @param isTop 是否置顶
     * @return 更新结果影响行数
     */
    @Update("UPDATE post SET is_essence = #{isEssence}, is_top = #{isTop} WHERE post_id = #{postId}")
    int updatePostStatus(@Param("postId") Long postId,
                         @Param("isEssence") boolean isEssence,
                         @Param("isTop") boolean isTop);
}