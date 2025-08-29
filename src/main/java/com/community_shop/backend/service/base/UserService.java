package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.VO.RegisterVO;
import com.community_shop.backend.VO.UserProfileVO;
import com.community_shop.backend.component.exception.UserException;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.component.exception.BaseException;

import java.util.List;

/**
 * 用户管理Service接口，实现《文档》中用户注册、登录、信用管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：用户注册登录（手机号/邮箱/第三方）、个人资料、信用体系
 * 2. 《文档4_数据库工作（新）.docx》：user表结构（user_id、username、credit_score等）
 * 3. 《代码文档1 Mapper层设计.docx》：UserMapper的CRUD及信用分更新方法
 */
public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    int addUser(User user);
    int updateUser(User user);
    int deleteUser(Long id);

    //更新用户状态
    boolean updateUserStatus(Long userId, String status);

    //更新用户角色
    boolean updateUserRole(Long userId, String role);

    //更新用户密码
    boolean updateUserPassword(Long userId, String password);

    //更新用户名称
    boolean updateUsername(Long userId, String username);

    //根据状态获取用户
    List<User> getUsersByStatus(String status, boolean isDESC, String order, Integer limit, Integer offset);

    //根据用户名获取用户
    List<User> getUsersByUsername(String username, boolean isDESC, String order, Integer limit, Integer offset);

    //根据用户角色获取用户
    List<User> getUsersByRole(String role, boolean isDESC, String order, Integer limit, Integer offset);


    //检验关键数据是否非法
    //若姓名、邮箱和电话非法，则抛出对应异常
    //针对add，update
    void checkFactor(User user) throws UserException;

    //根据userId检查用户是否存在
    //针对update
    void checkUserExists(Long userId) throws UserException;

    /**
     * 新增用户（基础CRUD）
     * 核心逻辑：密码BCrypt加密、初始化信用分100分，调用UserMapper.insert插入数据
     * @param user 用户实体（含username、email、password等基础字段，不含user_id）
     * @return ResultDTO<Integer> 成功返回新增用户ID，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#insert(User)
     */
    ResultDTO<Integer> insertUser(User user);

    /**
     * 按用户ID查询（基础CRUD）
     * 核心逻辑：调用UserMapper.selectById查询，对password字段脱敏处理
     * @param userId 用户ID（主键）
     * @return ResultDTO<User> 成功返回脱敏后的用户详情（含头像、信用分），失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#selectById(Long)
     */
    ResultDTO<User> selectUserById(Long userId);

    /**
     * 按手机号查询（基础CRUD）
     * 核心逻辑：调用UserMapper.selectByPhone查询，用于手机号登录校验
     * @param phoneNumber 手机号（唯一）
     * @return ResultDTO<User> 成功返回脱敏后的用户信息，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#selectByPhone(String)
     */
    ResultDTO<User> selectUserByPhone(String phoneNumber);

    /**
     * 更新用户资料（基础CRUD）
     * 核心逻辑：校验用户身份，调用UserMapper.updateById更新非敏感字段（昵称、头像等）
     * @param userId 用户ID（当前操作用户）
     * @param userProfileVO 资料更新参数（含昵称、头像、兴趣标签）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#updateById(User)
     */
    ResultDTO<Boolean> updateUserProfile(Long userId, UserProfileVO userProfileVO);

    /**
     * 按用户ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验管理员权限或本人注销权限，调用UserMapper.deleteById标记删除
     * @param userId 待删除用户ID
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#deleteById(Long)
     */
    ResultDTO<Boolean> deleteUserById(Long userId);

    /**
     * 用户注册（业务方法）
     * 核心逻辑：校验手机号/邮箱唯一性、验证码有效性，调用insertUser完成注册
     * @param registerVO 注册参数（手机号/邮箱、密码、验证码）
     * @return ResultDTO<String> 成功返回"注册成功"，失败返回错误信息（如"手机号已注册"）
     * @see #insertUser(User)
     * @see #selectUserByPhone(String)
     */
    ResultDTO<String> register(RegisterVO registerVO);

    /**
     * 第三方登录（业务方法）
     * 核心逻辑：调用第三方接口获取OpenID，未绑定则自动注册，生成登录Token
     * @param platform 第三方平台（微信/QQ，枚举值："WECHAT"、"QQ"）
     * @param code 第三方授权码（由前端获取）
     * @return ResultDTO<String> 成功返回登录Token，失败返回错误信息
     * @see com.community_shop.backend.config.OAuth2Config （适配《文档2》OAuth2.0集成）
     */
    ResultDTO<String> loginByThirdParty(String platform, String code);

    /**
     * 更新用户信用分（业务方法）
     * 核心逻辑：校验用户存在性，计算新信用分（最低0分），调用UserMapper.updateCreditScore更新
     * @param userId 用户ID
     * @param scoreChange 信用分变更值（正数增加，负数减少）
     * @param reason 变更原因（如"订单好评+5分"、"违规发帖-10分"）
     * @return ResultDTO<Integer> 成功返回更新后的信用分，失败返回错误信息
     * @see com.community_shop.backend.mapper.UserMapper#updateCreditScore(Long, Integer)
     * @see 《文档2_系统设计.docx》信用分规则
     */
    ResultDTO<Integer> updateCreditScore(Long userId, Integer scoreChange, String reason);
}
