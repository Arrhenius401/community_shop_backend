package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    //不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM user")
    int getCount();

    //使用邮箱获得用户ID
    @Select("SELECT userID FROM user where email = #{email};")
    Long getIDByEmail(String email);

    //使用电话号码获得用户ID
    @Select("SELECT userID FROM user where phoneNumber = #{phoneNumber};")
    Long getIDByPhoneNumber(String phoneNumber);

    //使用用户名获得用户ID
    @Select("SELECT userID FROM user where username = #{username};")
    Long getIDByUsername(String username);

    //使用ID获得用户密码
    @Select("SELECT password FROM user where userID = #{id};")
    String getPassword(Long id);

    //使用ID获得用户实例
    @Select("SELECT * FROM user where userID = #{id};")
    User getUserByID(Long id);

    //使用ID获得用户名称
    @Select("SELECT username FROM user where userID = #{id};")
    String getUsernameByID(Long id);

    //注册新用户,不显性设置userID和profilePicture
    @Insert("INSERT user(username, email, phoneNumber, password, initDate, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{initDate}, #{status});")
    void insertDefaultUser(String username, String email, String phoneNumber, String password,
                    String initDate, String role, String status);

    //添加用户
    @Insert("INSERT user(username, email, phoneNumber, password, initDate, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{initDate}, #{status});")
    int addUser(User user);

    //测试方法：总览
    @Select("SELECT * FROM user;")
    List<User> getAllUsers();

    //获得指定用户(限定比较指标，比较参数，排序指标，排序方向，数目和偏移量)
    //#{}: 参数可被自动转义，可防止SQL注入
    //用于替换SQL中的值（如 WHERE column = ?）
    //${}: 参数值会直接拼接到SQL中，有SQL注入风险
    //用于直接替换SQL片段（如列名、表名、排序方向等）
    @Select("SELECT * FROM user WHERE ${compareIndex} = #{compareParam}\n" +
            "ORDER BY ${order} ${direction}\n" +
            "LIMIT #{offset}, #{limit};")
    List<User> getUsersByAllParam(String compareIndex, String compareParam, String order,String direction, int limit, int offset);

    //更新用户信息
    @Update("UPDATE user SET username = #{username}, email = #{email}, phoneNumber = #{phoneNumber}, password = #{password}, role = #{role}, status = #{status} WHERE userID = #{userID}")
    int updateUser(User user);

    //更新用户名
    @Update("UPDATE user SET username = #{username} WHERE userid = #{id}")
    int updateUsername(String username, Long id);

    //更新角色
    @Update("UPDATE user SET role = #{role} WHERE userid = #{id}")
    int updateUserRole(String role, Long id);

    //更新密码
    @Update("UPDATE user SET password = #{password} WHERE userid = #{id}")
    int updateUserPassword(String password, Long id);

    //更新用户状态
    @Update("UPDATE homework_web.user SET status = #{status} where userID = #{userID}")
    int updateUserStatus(Long userID, String status);

    // 删除用户
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUser(Long id);
}
