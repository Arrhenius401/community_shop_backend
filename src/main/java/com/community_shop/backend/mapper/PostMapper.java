package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper {

    //不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM post")
    int getCount();

    //根据帖子ID获取帖子实例
    @Select("SELECT * FROM homework_web.post where postID = #{id};")
    Post getPostByID(Long id);

    //获得指定用户发布的所有帖子
    @Select("SELECT * FROM homework_web.post where userID = #{userID};")
    List<Post> getAllPostByUserID(Long userID);

    //获得所有帖子(不限状态)
    @Select("SELECT * FROM homework_web.post;")
    List<Post> getAllPost();

    //获得所有帖子(限定状态)
    @Select("SELECT * FROM homework_web.post WHERE status = #{status};")
    List<Post> getAllPostAddStatus(String status);

    //MyBatis不支持Java的重载特性
    //MyBatis 会把 Mapper 接口里的每个方法映射成一个唯一的 SQL 语句
    //映射的键由命名空间（也就是 Mapper 接口的全限定名）和方法名组合而成

    //获得所有帖子(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM homework_web.post WHERE status = #{status}\n" +
            "ORDER BY postID DESC\n" +
            "LIMIT #{limit};")
    List<Post> getAllPostAddLimit(String status, int limit);

    //获得所有帖子(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM homework_web.post WHERE status = #{status}\n" +
            "ORDER BY postID DESC\n" +
            "LIMIT #{offset}, #{limit};")
    List<Post> getAllPostAddOffset(String status, int offset, int limit);

    //获得所有帖子(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM homework_web.post WHERE status = #{status}\n" +
            "ORDER BY #{order} DESC\n" +
            "LIMIT #{offset}, #{limit};")
    List<Post> getAllPostByAllParam(String status, String order, int offset, int limit);

    //插入帖子实体
    @Insert("INSERT INTO homework_web.post (title, content, userID, likeCount, commentCount, createTime, status, ishot) \n" +
            "VALUES(#{title}, #{content}, #{userID}, #{likeCount}, #{commentCount}, #{createTime}, #{status}, #{ishot});")
    void insertPost(String title, String content, Long userID, int likeCount,
                           int commentCount, String createTime, String status, int ishot);

    //更新帖子状态
    @Update("UPDATE homework_web.post SET status = #{status} where postID = #{postID}")
    void updatePostStatus(Long postID, String status);
}
