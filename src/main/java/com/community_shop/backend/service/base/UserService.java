package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.user.*;
import com.community_shop.backend.dto.verification.VerifyEmailDTO;
import com.community_shop.backend.dto.verification.VerifyPhoneDTO;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.entity.User;
import org.springframework.stereotype.Service;

/**
 * 用户管理Service接口，实现《文档》中用户注册、登录、信用管理等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：用户注册登录（手机号/邮箱/第三方）、个人资料、信用体系
 * 2. 《文档4_数据库工作（新）.docx》：user表结构（user_id、username、credit_score等）
 * 3. 《代码文档1 Mapper层设计.docx》：UserMapper的CRUD及信用分更新方法
 */
@Service
public interface UserService extends BaseService<User>{

    /**
     * 用户注册（支持手机号/邮箱注册，含验证码校验）
     * @param registerDTO 注册请求参数DTO
     * @return true成功/false失败）
     */
    Boolean register(RegisterDTO registerDTO);

    /**
     * 用户邮箱注册（含验证码校验）
     * @param registerEmailDTO 注册请求参数DTO
     * @return true成功/false失败）
     */
    Boolean registerByEmail(RegisterEmailDTO registerEmailDTO);

    /**
     * 用户手机号注册（含验证码校验）
     * @param registerPhoneDTO 注册请求参数DTO
     * @return true成功/false失败）
     */
    Boolean registerByPhone(RegisterPhoneDTO registerPhoneDTO);

    /**
     * 绑定邮箱（含验证码校验）
     * @param verifyDTO 绑定邮箱请求参数DTO
     * @return true成功/false失败）
     */
    Boolean bindEmail(VerifyEmailDTO verifyDTO);

    /**
     * 绑定手机号（含验证码校验）
     * @param verifyDTO 绑定手机号请求参数DTO
     * @return true成功/false失败）
     */
    Boolean bindPhone(VerifyPhoneDTO verifyDTO);

    /**
     * 忘记密码后重置密码（手机号）
     * @param verifyDTO 忘记密码请求参数DTO
     * @return true成功/false失败）
     */
    Boolean updatePasswordByEmail(PasswordUpdateEmailDTO verifyDTO);

    /**
     * 忘记密码后重置密码（邮箱）
     * @param verifyDTO 忘记密码请求参数DTO
     * @return true成功/false失败）
     */
    Boolean updatePasswordByPhone(PasswordUpdatePhoneDTO verifyDTO);

    /**
     * 用户登录（支持账号密码/手机号验证码登录）
     * @param loginDTO 登录请求参数DTO
     * @return 包含Token的登录结果DTO
     */
    LoginResultDTO login(LoginDTO loginDTO);

    /**
     * 第三方账号登录（微信/QQ/支付宝）
     * @param thirdPartyLoginDTO 第三方登录请求参数DTO
     * @return 包含Token的登录结果DTO
     */
    LoginResultDTO loginByThirdParty(ThirdPartyLoginDTO thirdPartyLoginDTO);

    /**
     * 根据用户ID查询详情（敏感字段脱敏）
     * @param userId 用户唯一标识
     * @return 脱敏后的用户详情DTO
     */
    UserDetailDTO selectUserById(Long userId);

    /**
     * 多条件分页查询用户列表
     * @param userQueryDTO 包含分页参数和筛选条件的查询DTO
     * @return 分页用户列表结果
     */
    PageResult<UserListItemDTO> queryUsers(UserQueryDTO userQueryDTO);

    /**
     * 统计用户数量（支持多条件筛选）
     * @param userQueryDTO 筛选条件DTO
     * @return 用户数量
     */
    int countUsers(UserQueryDTO userQueryDTO);

    /**
     * 更新用户资料（仅支持昵称、头像等非敏感字段）
     * @param userId 操作用户ID
     * @param profileDTO 资料更新请求参数DTO
     * @return 更新后的用户详情DTO
     */
    UserDetailDTO updateProfile(Long userId, UserProfileUpdateDTO profileDTO);

    /**
     * 修改用户密码（需校验原密码）
     * @param passwordDTO 密码更新请求参数DTO
     * @return 操作结果（true成功/false失败）
     */
    Boolean updatePassword(PasswordUpdateDTO passwordDTO);

    /**
     * 调整用户信用分（支持增减，最低为0）
     * @param userId 用户ID
     * @param scoreChange 分数变更值（正数增加/负数减少）
     * @param reason true成功/false失败）
     * @return 调整后的信用分
     */
    Boolean updateCreditScore(Long userId, Integer scoreChange, String reason);

    /**
     * 更新用户角色（管理员操作）
     * @param operatorId 操作人ID
     * @param userId 用户ID
     * @param role 目标角色枚举
     * @return 操作结果（true成功/false失败）
     */
    Boolean updateUserRole(Long operatorId, Long userId, UserRoleEnum role);

    /**
     * 更新用户账号状态（启用/禁用/注销）
     * @param operatorId 操作人ID
     * @param userId 用户ID
     * @param status 目标状态枚举
     * @return 操作结果（true成功/false失败）
     */
    Boolean updateUserStatus(Long operatorId, Long userId, UserStatusEnum status);

    /**
     * 验证密码（业务方法）
     * @param userId
     * @param rawPassword 明文密码
     * @return 验证结果（true/false）
     */
    Boolean verifyPassword(Long userId, String rawPassword);

    /**
     * 验证用户角色（业务方法）
     * @param userId
     * @param role
     * @return 验证结果（true/false）
     */
    Boolean verifyRole(Long userId, UserRoleEnum role);

}
