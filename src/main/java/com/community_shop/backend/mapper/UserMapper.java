package com.community_shop.backend.mapper;

import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户管理模块Mapper接口，对应user表操作
 */
@Mapper
public interface UserMapper {
    // SQL知识补充
    // CONCAT() 是 SQL 标准函数，是一个字符串函数，用于将两个或多个字符串连接（拼接）成一个字符串。它的基本作用是将多个字符串参数合并为一个单一的字符串结果。
    // LIKE 是一个用于字符串匹配的运算符，主要用于 WHERE 子句中，判断某列的值是否符合指定的字符串模式。它通常与通配符配合使用，实现模糊查询。
    // _: 匹配恰好一个任意字符
    // %: 匹配任意字符序列

    // #{}: 参数可被自动转义，可防止SQL注入
    // 用于替换SQL中的值（如 WHERE column = ?）
    // ${}: 参数值会直接拼接到SQL中，有SQL注入风险
    // 用于直接替换SQL片段（如列名、表名、排序方向等）

    // SELECT语句
    // 不必写实现类，注解已实现改函数
    @Select("SELECT COUNT(1) FROM user")
    int getCount();

    // 总览
    @Select("SELECT * FROM user;")
    List<User> getAllUsers();

    // 使用ID获得用户名称
    @Select("SELECT username FROM user where user_id = #{id};")
    String getUsernameByID(Long id);


    // INSERT语句
    // 注册新用户,不显性设置userID和profilePicture
    @Insert("INSERT user(username, email, phone_number, password, create_time, status) \n" +
            "values(#{username}, #{email}, #{phoneNumber}, #{password},  #{createTime}, #{status});")
    void insertDefaultUser(User user);

    // 更新密码
    @Update("UPDATE user SET password = #{password} WHERE user_id = #{id}")
    int updateUserPassword(String password, Long id);

    // 更新用户状态
    @Update("UPDATE homework_web.user SET status = #{status} where user_id = #{userID}")
    int updateUserStatus(Long userID, UserStatusEnum status);

    // 更新角色
    @Update("UPDATE user SET role = #{role} WHERE user_id = #{id}")
    int updateUserRole(UserRoleEnum role, Long id);


    // ==================== 基础CRUD ====================
    /**
     * 插入用户（注册功能）
     * @param user 用户实体（含用户名、邮箱、手机号、密码等核心信息）
     * @return 影响行数（1=成功，0=失败）
     */
    int insert(User user);

    /**
     * 通过用户ID查询用户详情
     * @param userId 用户唯一标识
     * @return 用户完整实体（含信用分、发帖数等统计字段）
     */
    User selectById(@Param("userId") Long userId);

    /**
     * 通过用户ID更新用户资料
     * @param user 用户实体（含需更新的字段：用户名、邮箱、头像等）
     * @return 影响行数
     */
    int updateById(User user);

    /**
     * 通过用户ID注销账号（删除核心数据）
     * @param userId 用户唯一标识
     * @return 影响行数
     */
    int deleteById(@Param("userId") Long userId);

    // ==================== 登录与身份校验 ====================
    /**
     * 通过用户名查询用户（账号密码登录场景）
     * @param username 登录用户名
     * @return 用户实体（含密码用于校验）
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 通过手机号查询用户（手机号+验证码登录场景）
     * @param phoneNumber 用户手机号
     * @return 用户实体
     */
    User selectByPhone(@Param("phoneNumber") String phoneNumber);

    /**
     * 通过邮箱查询用户（邮箱登录/找回密码场景）
     * @param email 用户邮箱
     * @return 用户实体
     */
    User selectByEmail(@Param("email") String email);


    // ==================== 信用分与统计 ====================
    /**
     * 更新用户信用分
     * @param userId 用户ID
     * @param score 调整后的信用分（可增可减）
     * @return 影响行数
     */
    int updateCreditScore(@Param("userId") Long userId, @Param("score") Integer score);

    /**
     * 更新用户发帖数（自增1）
     * @param userId 用户ID
     * @return 影响行数
     */
    int updatePostCount(@Param("userId") Long userId);

    /**
     * 更新用户粉丝数
     * @param userId 用户ID
     * @param count 调整后的粉丝数（可增可减）
     * @return 影响行数
     */
    int updateFollowerCount(@Param("userId") Long userId, @Param("count") int count);


    // ==================== 条件查询 ====================
    /**
     * 通过兴趣标签查询用户（社交推荐场景）
     * @param tags 兴趣标签列表（如["数码","运动"]）
     * @return 符合条件的用户列表
     */
    List<User> selectByInterestTags(@Param("tags") List<String> tags);

    /**
     * 多条件组合查询用户
     * @param compareIndex 筛选字段（如"credit_score"、"post_count"）
     * @param compareParam 筛选参数（如"100"、"5"）
     * @param order 排序字段（如"create_time"、"follower_count"）
     * @param direction 排序方向（ASC/DESC）
     * @param limit 每页条数
     * @param offset 偏移量（从第几条开始查询）
     * @return 符合条件的用户分页列表
     */
    List<User> getUsersByAllParam(
            @Param("compareIndex") String compareIndex,
            @Param("compareParam") String compareParam,
            @Param("order") String order,
            @Param("direction") String direction,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 按角色查询用户（管理员权限管理场景）
     * @param role 用户角色枚举（USER/ADMIN）
     * @return 符合角色的用户列表
     */
    List<User> selectByRole(@Param("role") UserRoleEnum role);
}
