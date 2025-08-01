package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserMapper {

    //不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM user")
    int getCount();

    //使用邮箱获得用户ID
    @Select("SELECT userID FROM homework_web.user where email = #{email};")
    Long getIDByEmail(String email);

    //使用电话号码获得用户ID
    @Select("SELECT userID FROM homework_web.user where phoneNumber = #{phoneNumber};")
    Long getIDByPhoneNumber(Long phoneNumber);

    //使用ID获得用户密码
    @Select("SELECT password FROM homework_web.user where userID = #{id};")
    String getPassword(Long id);

    //使用ID获得用户实例
    @Select("SELECT * FROM homework_web.user where userID = #{id};")
    User getUserByID(Long id);

    //使用ID获得用户名称
    @Select("SELECT username FROM homework_web.user where userID = #{id};")
    String getUsernameByID(Long id);

    //注册新用户,不显性设置userID和profilePicture
    @Insert("INSERT homework_web.user(username, email, phoneNumber, password, initDate, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{initDate}, #{status});")
    void insertDefaultUser(String username, String email, Long phoneNumber, String password,
                    String initDate, String role, String status);

    //更新用户名
    @Update("UPDATE homework_web.user SET username = #{username} WHERE userid = #{id}")
    void updateUsername(String username, Long id);

    //更新角色
    @Update("UPDATE homework_web.user SET role = #{role} WHERE userid = #{id}")
    void updateRole(String role, Long id);

    //更新密码
    @Update("UPDATE homework_web.user SET password = #{password} WHERE userid = #{id}")
    void updatePassword(String password, Long id);

    //测试方法：总览
    @Select("SELECT * FROM homework_web.user;")
    List<User> getAllUser();

    //获得所有用户(限定状态)
    @Select("SELECT * FROM homework_web.user WHERE status = #{status}\n" +
            "ORDER BY userID DESC;")
    List<User> getAllUserAddStatus(String status);

    //获得所有用户(限定状态和数目)
    @Select("SELECT * FROM homework_web.user WHERE status = #{status}\n" +
            "ORDER BY userID DESC\n" +
            "LIMIT #{limit};")
    List<User> getAllUserAddLimit(String status, int limit);

    //获得所有用户(限定状态，数目和偏移量)
    @Select("SELECT * FROM homework_web.user WHERE status = #{status}\n" +
            "ORDER BY userID DESC\n" +
            "LIMIT #{offset}, #{limit};")
    List<User> getAllUserAddOffset(String status, int offset, int limit);

    //获得所有用户(限定状态，指标，数目和偏移量)
    @Select("SELECT * FROM homework_web.user WHERE status = #{status}\n" +
            "ORDER BY #{order} DESC\n" +
            "LIMIT #{offset}, #{limit};")
    List<User> getAllUserByAllParam(String status, String order, int offset, int limit);

    //更新用户状态
    @Update("UPDATE homework_web.user SET status = #{status} where userID = #{userID}")
    void updateUserStatus(Long userID, String status);
}
