package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    //MyBatis不支持Java的重载特性
    //MyBatis 会把 Mapper 接口里的每个方法映射成一个唯一的 SQL 语句
    //映射的键由命名空间（也就是 Mapper 接口的全限定名）和方法名组合而成

    //不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM post")
    int getCount();

    //根据帖子ID获取帖子实例
    @Select("SELECT * FROM post where postID = #{id};")
    Post getPostByID(Long id);

    //获得指定用户发布的所有帖子
    @Select("SELECT * FROM post where userID = #{userID};")
    List<Post> getAllPostByUserID(Long userID);

    //获得所有帖子(不限状态)
    @Select("SELECT * FROM post;")
    List<Post> getAllPost();

    //获得所有帖子(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM post WHERE status = #{status}\n" +
            "ORDER BY postID DESC\n" +
            "LIMIT #{limit};")
    List<Post> getAllPostAddLimit(String status, int limit);

    //获得指定用户(限定比较指标，比较参数，排序指标，排序方向，数目和偏移量)
    //#{}: 参数可被自动转义，可防止SQL注入
    //用于替换SQL中的值（如 WHERE column = ?）
    //${}: 参数值会直接拼接到SQL中，有SQL注入风险
    //用于直接替换SQL片段（如列名、表名、排序方向等）
    @Select("SELECT * FROM post WHERE ${compareIndex} = #{compareParam}\n" +
            "ORDER BY ${order} ${direction}\n" +
            "LIMIT #{offset}, #{limit};")
    List<Post> getPostsByAllParam(String compareIndex, String compareParam, String order,String direction, int limit, int offset);

    //插入帖子实体
    @Insert("INSERT INTO post (title, content, userID, likeCount, commentCount, createTime, status, ishot) \n" +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{commentCount}, #{createTime}, #{status}, #{ishot});")
    int addPost(Post post);

    //更新帖子状态
    @Update("UPDATE post SET status = #{status} where postID = #{postID}")
    void updatePostStatus(Long postID, String status);

    //更新帖子实体
    @Update("UPDATE post SET title = #{title}, content = #{content}, userID = #{userID}, likeCount = #{likeCount}, commentCount = #{commentCount}, createTime = #{createTime}, status = #{status}, ishot = #{ishot} where postID = #{postID}")
    int updatePost(Post post);

    //删除指定帖子
    @Delete("DELETE FROM post WHERE postID = #{id}")
    int deletePost(Long id);
}
