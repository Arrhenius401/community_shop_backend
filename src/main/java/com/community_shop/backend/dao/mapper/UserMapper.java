package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.dto.user.UserQueryDTO;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户管理模块Mapper接口，对应user表操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * SQL知识补充1
     * CONCAT() 是 SQL 标准函数，是一个字符串函数，用于将两个或多个字符串连接（拼接）成一个字符串。它的基本作用是将多个字符串参数合并为一个单一的字符串结果。
     * LIKE 是一个用于字符串匹配的运算符，主要用于 WHERE 子句中，判断某列的值是否符合指定的字符串模式。它通常与通配符配合使用，实现模糊查询。
     * _: 匹配恰好一个任意字符
     * %: 匹配任意字符序列
     *
     * SQL知识补充1
     * #{}: 参数可被自动转义，可防止SQL注入
     * 用于替换SQL中的值（如 WHERE column = ?）
     * ${}: 参数值会直接拼接到SQL中，有SQL注入风险
     * 用于直接替换SQL片段（如列名、表名、排序方向等）
     */

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
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 影响行数
     */
    @Update("UPDATE `user` SET password = #{newPassword} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    /**
     * 绑定邮箱
     * @param userId 用户ID
     * @param email 邮箱
     * @return 影响行数
     */
    @Update("UPDATE `user` SET email = #{email} WHERE user_id = #{userId}")
    int updateEmail(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 绑定手机号
     * @param userId 用户ID
     * @param phoneNumber 手机号
     * @return 影响行数
     */
    @Update("UPDATE `user` SET phone_number = #{phoneNumber} WHERE user_id = #{userId}")
    int updatePhoneNumber(@Param("userId") Long userId, @Param("phoneNumber") String phoneNumber);

    /**
     * 更新用户状态
     * @param userID 用户ID
     * @param status 状态枚举（NORMAL/BANNED）
     * @return 影响行数
     */
    @Update("UPDATE `user` SET status = #{status} where user_id = #{userID}")
    int updateUserStatus(Long userID, UserStatusEnum status);

    /**
     * 更新用户角色
     * @param role 角色枚举（USER/ADMIN）
     * @param id 用户ID
     * @return 更新行数
     */
    @Update("UPDATE `user` SET role = #{role} WHERE user_id = #{id}")
    int updateUserRole(UserRoleEnum role, Long id);

    /**
     * 获取用户总数
     * @return 用户总数
     */
    @Select("SELECT COUNT(1) FROM `user`")
    int getCount();

    // ==================== 条件查询 ====================

    /**
     * 按角色查询用户（管理员权限管理场景）
     * @param role 用户角色枚举（USER/ADMIN）
     * @return 符合角色的用户列表
     */
    List<User> selectByRole(@Param("role") UserRoleEnum role);

    /**
     * 多条件组合查询用户总数（适配分页查询的总记录数统计）
     * @param userQueryDTO 分页查询参数（含兴趣标签、筛选字段、筛选值等条件）
     * @return 符合条件的用户总数
     */
    int countByQuery(UserQueryDTO userQueryDTO);

    /**
     * 多条件组合查询用户列表（适配UserQueryDTO，支持兴趣标签、筛选、排序、分页）
     * @param userQueryDTO 分页查询参数（含兴趣标签、筛选字段、筛选值、排序字段、分页信息）
     * @return 符合条件的用户分页列表
     */
    List<User> selectByQuery(UserQueryDTO userQueryDTO);

}
