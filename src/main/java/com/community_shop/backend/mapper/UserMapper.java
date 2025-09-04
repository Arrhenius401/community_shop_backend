package com.community_shop.backend.mapper;

import com.community_shop.backend.component.enums.UserRoleEnum;
import com.community_shop.backend.component.enums.UserStatusEnum;
import com.community_shop.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    // SQL知识补充
    // CONCAT() 是 SQL 标准函数，是一个字符串函数，用于将两个或多个字符串连接（拼接）成一个字符串。它的基本作用是将多个字符串参数合并为一个单一的字符串结果。
    // LIKE 是一个用于字符串匹配的运算符，主要用于 WHERE 子句中，判断某列的值是否符合指定的字符串模式。它通常与通配符配合使用，实现模糊查询。
    // _: 匹配恰好一个任意字符
    // %: 匹配任意字符序列

    // SELECT语句
    // 不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM user")
    int getCount();

    // 总览
    @Select("SELECT * FROM user;")
    List<User> getAllUsers();

    // 使用邮箱获得用户ID
    @Select("SELECT user_id FROM user where email = #{email};")
    Long getIDByEmail(String email);

    // 使用电话号码获得用户ID
    @Select("SELECT user_id FROM user where phone_number = #{phoneNumber};")
    Long getIDByPhoneNumber(String phoneNumber);

    // 使用用户名获得用户ID
    @Select("SELECT user_id FROM user where username = #{username};")
    Long getIDByUsername(String username);

    // 使用ID获得用户密码
    @Select("SELECT password FROM user where user_id = #{id};")
    String getPassword(Long id);

    // 使用ID获得用户实例
    @Select("SELECT * FROM user where user_id = #{id};")
    User getUserByID(Long id);

    // 使用ID获得用户名称
    @Select("SELECT username FROM user where user_id = #{id};")
    String getUsernameByID(Long id);

    // 获得指定用户(限定比较指标，比较参数，排序指标，排序方向，数目和偏移量)
    // #{}: 参数可被自动转义，可防止SQL注入
    // 用于替换SQL中的值（如 WHERE column = ?）
    // ${}: 参数值会直接拼接到SQL中，有SQL注入风险
    // 用于直接替换SQL片段（如列名、表名、排序方向等）
    @Select("SELECT * FROM user WHERE ${compareIndex} = #{compareParam}\n" +
            "ORDER BY ${order} ${direction}\n" +
            "LIMIT #{offset}, #{limit};")
    List<User> getUsersByAllParam(String compareIndex, String compareParam, String order,String direction, int limit, int offset);


    // INSERT语句
    // 注册新用户,不显性设置userID和profilePicture
    @Insert("INSERT user(username, email, phone_number, password, init_date, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{initDate}, #{status});")
    void insertDefaultUser(String username, String email, String phoneNumber, String password,
                           String initDate, String role, String status);

    // 添加用户
    @Insert("INSERT user(username, email, phone_number, password, init_date, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{initDate}, #{status});")
    int addUser(User user);


    // UPDATE语句
    // 更新用户名
    @Update("UPDATE user SET username = #{username} WHERE user_id = #{id}")
    int updateUsername(String username, Long id);

    // 更新密码
    @Update("UPDATE user SET password = #{password} WHERE user_id = #{id}")
    int updateUserPassword(String password, Long id);

    // 更新用户状态
    @Update("UPDATE homework_web.user SET status = #{status} where user_id = #{userID}")
    int updateUserStatus(Long userID, UserStatusEnum status);

    // 更新角色
    @Update("UPDATE user SET role = #{role} WHERE user_id = #{id}")
    int updateUserRole(UserRoleEnum role, Long id);


    // DELETE语句
    // 删除用户
    @Delete("DELETE FROM users WHERE user_id = #{id}")
    int deleteUser(Long id);


    // 基础CRUD
    /**
     * 插入用户（注册功能）
     *
     * @param user 用户对象
     * @return 插入成功的记录数
     */
    @Insert("INSERT INTO user(username, email, phone_number, password, init_date, status) " +
            "VALUES(#{username}, #{email}, #{phoneNumber}, #{password}, #{initDate}, #{status})")
    int insert(User user);

    /**
     * 通过用户 ID 查询用户详情（个人中心）
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User selectById(Long userId);

    /**
     * 更新用户资料（如头像、昵称、隐私设置）
     *
     * @param user 用户对象
     * @return 更新成功的记录数
     */
    @Update("UPDATE user SET username = #{username}, email = #{email}, phone_number = #{phoneNumber}, " +
            "profile_picture = #{profilePicture}, bio = #{bio}, role = #{role}, status = #{status} " +
            "WHERE user_id = #{userID}")
    int updateById(User user);

    /**
     * 注销账号（非核心，但预留接口）
     *
     * @param userId 用户ID
     * @return 删除成功的记录数
     */
    @Delete("DELETE FROM user WHERE user_id = #{userId}")
    int deleteById(Long userId);

    // 登录与身份校验
    /**
     * 通过用户名查询（账号密码登录）
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    /**
     * 通过手机号查询（手机号 + 验证码登录）
     *
     * @param phoneNumber 手机号
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE phone_number = #{phoneNumber}")
    User selectByPhone(String phoneNumber);

    /**
     * 通过邮箱查询（邮箱登录）
     *
     * @param email 邮箱
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);

    // 信用分与统计
    /**
     * 更新用户信用分（信用体系）
     *
     * @param userId 用户ID
     * @param score  新的信用分数
     * @return 更新成功的记录数
     */
    @Update("UPDATE user SET credit_score = #{score} WHERE user_id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("score") Integer score);

    /**
     * 更新用户发帖数（发帖后同步）
     *
     * @param userId 用户ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE user SET post_count = post_count + 1 WHERE user_id = #{userId}")
    int updatePostCount(Long userId);

    // 条件查询
    /**
     * 通过兴趣标签查询用户（可选社交功能）
     *
     * @param tags 兴趣标签列表
     * @return 用户列表
     */
    @Select({
            "<script>",
            "SELECT DISTINCT u.* FROM user u",
            "JOIN user_interest ui ON u.user_id = ui.user_id",
            "WHERE ui.interest_tag IN",
            "<foreach item='tag' collection='tags' open='(' separator=',' close=')'>",
            "#{tag}",
            "</foreach>",
            "</script>"
    })
    List<User> selectByInterestTags(@Param("tags") List<String> tags);
}
