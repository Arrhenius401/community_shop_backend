package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.UserConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.user.*;
import com.community_shop.backend.enums.SimpleEnum.LoginTypeEnum;
import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.base.UserThirdPartyService;
import com.community_shop.backend.utils.ThirdPartyAuthUtil;
import com.community_shop.backend.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// "object == null" 与 “Objects.isNull(object)” 等价
@Slf4j
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    /**
     * 指定事务的隔离级别为 READ_COMMITTED（读已提交）
     * 指定哪些异常发生时，事务需要「回滚」（即撤销方法中已执行的数据库操作）
     */

    // 信用分常量
    private static final Integer INIT_CREDIT_SCORE = 100; // 初始信用分
    private static final Integer MIN_CREDIT_SCORE = 0; // 最低信用分

    // 缓存相关常量
    private static final String CACHE_KEY_USER = "user:info:"; // 用户信息缓存Key前缀
    private static final String CACHE_KEY_USER_LIST = "user:list:"; // 用户列表缓存Key前缀
    private static final long CACHE_TTL_USER = 60; // 用户信息缓存有效期（分钟）
    private static final long CACHE_TTL_USER_LIST = 30; // 用户列表缓存有效期（分钟）

    @Value("${jwt.expiration}")
    private static long TOKEN_EXPIRATION_MILLISECONDS;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserThirdPartyService userThirdPartyService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ThirdPartyAuthUtil thirdPartyAuthUtil;

    @Autowired
    private UserThirdPartyMapper userThirdPartyMapper;

    @Autowired
    private UserConvert userConvert;


    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(RegisterDTO registerDTO) {

        try {
            // 1. 参数校验（匹配RegisterDTO校验规则）
            validateRegisterParam(registerDTO);

            // 2. 校验手机号/邮箱唯一性
            if (StringUtils.hasText(registerDTO.getPhoneNumber())
                    && userMapper.selectByPhone(registerDTO.getPhoneNumber()) != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
            if (StringUtils.hasText(registerDTO.getEmail())
                    && userMapper.selectByEmail(registerDTO.getEmail()) != null) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }

            // 3. 校验验证码（匹配文档“注册校验”要求）
            if (!StringUtils.hasText(registerDTO.getVerifyCode())
                    || !registerDTO.getVerifyCode().matches("^\\d{6}$")) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
            }

            // 4. 构建User实体（密码加密+初始化基础数据）
            User user = userConvert.registerDtoToUser(registerDTO);
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 密码加密存储（文档要求）
            user.setStatus(UserStatusEnum.NORMAL);
            user.setRole(UserRoleEnum.USER);
            user.setCreateTime(LocalDateTime.now());

            // 5. 插入数据库（匹配UserMapper.insert方法）
            int insertRows = userMapper.insert(user);
            if (insertRows <= 0) {
                log.error("用户注册失败，注册信息：{}", registerDTO);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 5. 缓存用户信息
            UserDetailDTO userDetailDTO = userConvert.userToUserDetailDTO(user);
            redisTemplate.opsForValue().set(
                    CACHE_KEY_USER + user.getUserId(),
                    userDetailDTO,
                    CACHE_TTL_USER,
                    TimeUnit.MINUTES
            );

            log.info("用户注册成功，用户ID：{}", user.getUserId());
            return true;

        } catch (BusinessException e) {
          throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
        }

    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public LoginResultDTO login(LoginDTO loginDTO) {
        try {
            // 1. 参数校验（匹配LoginDTO非空规则）
            if (loginDTO == null || loginDTO.getLoginType() == null
                    || !StringUtils.hasText(loginDTO.getLoginId())
                    || !StringUtils.hasText(loginDTO.getCredential())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            User user = null;

            // 2. 按登录类型校验（匹配LoginDTO的loginType枚举）
            LoginTypeEnum loginType = loginDTO.getLoginType();
            switch (loginType) {
                case LoginTypeEnum.EMAIL:
                    // 账号密码登录（匹配UserMapper.selectByUsername）
                    user = userMapper.selectByEmail(loginDTO.getLoginId());
                    validatePassword(user, loginDTO.getCredential());
                    break;
                case LoginTypeEnum.PHONE_NUMBER:
                    // 手机号登录（匹配UserMapper.selectByPhone）
                    user = userMapper.selectByPhone(loginDTO.getLoginId());
                    validatePassword(user, loginDTO.getCredential());

//                    // 验证码校验
//                    if (!StringUtils.hasText(loginDTO.getVerifyCode())
//                            || !loginDTO.getVerifyCode().matches("^\\d{6}$")) {
//                        throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
//                    }
                    break;
                default:
                    throw new BusinessException(ErrorCode.PARAM_ERROR);
            }

            // 3. 生成JWT Token及过期时间（匹配LoginResultDTO结构）
            String token = tokenUtil.generateToken(user.getUserId());
            LocalDateTime tokenExpireTime = tokenUtil.getExpirationTimeFromToken(token);

            // 4. 封装登录结果
            LoginResultDTO loginResultDTO = new LoginResultDTO();
            LoginResultDTO.UserSimpleDTO userSimpleDTO = new LoginResultDTO.UserSimpleDTO();
            userSimpleDTO.setUserId(user.getUserId());
            userSimpleDTO.setUsername(user.getUsername());
            userSimpleDTO.setAvatarUrl(user.getAvatarUrl());
            userSimpleDTO.setCreditScore(user.getCreditScore());
            loginResultDTO.setUserInfo(userSimpleDTO);
            loginResultDTO.setToken(token);
            loginResultDTO.setTokenExpireTime(tokenExpireTime);

            // 6. 缓存用户信息
            Long expiration = tokenUtil.getExpiration();
            redisTemplate.opsForValue().set(CACHE_KEY_USER + user.getUserId(), user, expiration, TimeUnit.MILLISECONDS);
            log.info("用户登录成功，用户ID：{}，登录类型：{}", user.getUserId(), loginType);
            return loginResultDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }


    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public LoginResultDTO loginByThirdParty(ThirdPartyLoginDTO thirdPartyLoginDTO) {
        try {
            // 1. 参数校验（匹配ThirdPartyLoginDTO非空规则）
            if (thirdPartyLoginDTO == null || !StringUtils.hasText(thirdPartyLoginDTO.getThirdType())
                    || !StringUtils.hasText(thirdPartyLoginDTO.getAuthCode())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 解析第三方平台类型（匹配ThirdPartyTypeEnum）
            ThirdPartyTypeEnum thirdType = ThirdPartyTypeEnum.valueOf(thirdPartyLoginDTO.getThirdType());

            // 3. 用authCode获取OpenID和accessToken（模拟第三方接口调用）
            String openid = "OPENID_" + thirdType.name() + "_" + System.currentTimeMillis();
            String accessToken = "TOKEN_" + thirdType.name() + "_" + System.currentTimeMillis();

            // 4. 查询绑定关系（匹配UserThirdPartyMapper.selectByThirdTypeAndOpenid）
            UserThirdParty binding = userThirdPartyMapper.selectByThirdTypeAndOpenid(thirdType, openid);
            User user;

            if (binding != null && binding.getIsValid() == 1) {
                // 已绑定：查询用户信息
                user = userMapper.selectById(binding.getUserId());
            } else {
                // 未绑定：自动注册并绑定
                user = autoRegisterThirdPartyUser(thirdType, openid);
                bindThirdPartyAccount(thirdType, user.getUserId(), openid, accessToken);
            }

            // 5. 生成JWT Token及过期时间（匹配LoginResultDTO结构）
            String token = tokenUtil.generateToken(user.getUserId());
            LocalDateTime tokenExpireTime = tokenUtil.getExpirationTimeFromToken(token);

            // 6. 封装登录结果
            LoginResultDTO loginResultDTO = new LoginResultDTO();
            LoginResultDTO.UserSimpleDTO userSimpleDTO = new LoginResultDTO.UserSimpleDTO();
            userSimpleDTO.setUserId(user.getUserId());
            userSimpleDTO.setUsername(user.getUsername());
            userSimpleDTO.setAvatarUrl(user.getAvatarUrl());
            userSimpleDTO.setCreditScore(user.getCreditScore());
            loginResultDTO.setUserInfo(userSimpleDTO);
            loginResultDTO.setToken(token);
            loginResultDTO.setTokenExpireTime(tokenExpireTime);
            return loginResultDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("第三方平台登录失败", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }
    }

    @Override
    public UserDetailDTO selectUserById(Long userId) {
        try {
            // 1. 参数校验
            if (userId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 查询缓存
            User user = (User) redisTemplate.opsForValue().get(CACHE_KEY_USER + userId);
            if (Objects.nonNull(user)) {
                return userConvert.userToUserDetailDTO(user);
            }

            // 3. 查询数据库（匹配UserMapper.selectById）
            user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 4. 缓存并返回DTO
            redisTemplate.opsForValue().set(CACHE_KEY_USER + userId, user, TOKEN_EXPIRATION_MILLISECONDS, TimeUnit.HOURS);
            return userConvert.userToUserDetailDTO(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询用户信息失败", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailDTO updateProfile(Long userId, UserProfileUpdateDTO profileDTO) {
        try {
            // 1. 参数校验（匹配UserProfileUpdateDTO规则）
            if (userId == null || profileDTO == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            if (StringUtils.hasText(profileDTO.getUsername()) && (profileDTO.getUsername().length() < 1 || profileDTO.getUsername().length() > 10)) {
                throw new BusinessException(ErrorCode.USERNAME_LENGTH_INVALID);
            }
            if (StringUtils.hasText(profileDTO.getAvatarUrl()) && !profileDTO.getAvatarUrl().matches("^https?://.+$")) {
                throw new BusinessException(ErrorCode.AVATAR_URL_FORMAT_INVALID);
            }

            // 2. 校验用户存在
            User existingUser = userMapper.selectById(userId);
            if (existingUser == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 构建更新实体（仅更新允许修改的字段）
            User updateUser = new User();
            updateUser.setUserId(userId);
            updateUser.setUsername(profileDTO.getUsername());
            updateUser.setAvatarUrl(profileDTO.getAvatarUrl());
            updateUser.setBio(profileDTO.getBio());

            // 4. 执行更新（匹配UserMapper.updateById）
            int updateRows = userMapper.updateById(updateUser);
            if (updateRows <= 0) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 刷新缓存并返回
            User updatedUser = userMapper.selectById(userId);
            redisTemplate.opsForValue().set(CACHE_KEY_USER + userId, updatedUser, TOKEN_EXPIRATION_MILLISECONDS, TimeUnit.HOURS);
            return userConvert.userToUserDetailDTO(updatedUser);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(PasswordUpdateDTO passwordDTO) {
        try {
            // 1. 参数校验（匹配PasswordUpdateDTO规则）
            if (passwordDTO == null || !StringUtils.hasText(passwordDTO.getOldPassword())
                    || !StringUtils.hasText(passwordDTO.getNewPassword())
                    || !StringUtils.hasText(passwordDTO.getConfirmPassword())) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
            }
            if (!passwordDTO.getNewPassword().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")) {
                throw new BusinessException(ErrorCode.PASSWORD_FORMAT_INVALID);
            }

            // 2. 此处简化用户ID获取（实际从Token解析）
            Long userId = 1L;
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 校验原密码
            if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
            }

            // 4. 更新密码（匹配UserMapper.updatePassword）
            String encryptedNewPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
            int updateRows = userMapper.updatePassword(userId, encryptedNewPassword);
            if (updateRows <= 0) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 清除缓存
            redisTemplate.delete(CACHE_KEY_USER + userId);
            log.info("密码更新成功，用户ID：{}", userId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户密码失败", e);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCreditScore(Long userId, Integer scoreChange, String reason) {
        try {
            // 1. 参数校验
            if (userId == null || scoreChange == null || !StringUtils.hasText(reason)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验用户存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 计算新信用分（不低于0分）
            int newScore = Math.max(user.getCreditScore() + scoreChange, MIN_CREDIT_SCORE);

            // 4. 执行更新（匹配UserMapper.updateCreditScore）
            int updateRows = userMapper.updateCreditScore(userId, newScore);
            if (updateRows <= 0) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 5. 刷新缓存
            user.setCreditScore(newScore);
            redisTemplate.opsForValue().set(CACHE_KEY_USER + userId, user, TOKEN_EXPIRATION_MILLISECONDS, TimeUnit.HOURS);
            log.info("信用分更新成功，用户ID：{}，原分数：{}，新分数：{}，原因：{}", userId, user.getCreditScore(), newScore, reason);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信用分失败", e);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }
    }

    @Override
    public PageResult<UserDetailDTO> selectUserList(UserQueryDTO userQueryDTO) {
        // 1. 参数校验（UserQueryDTO继承PageParam，默认pageNum=1，pageSize=10）
        if (userQueryDTO == null) {
            userQueryDTO = new UserQueryDTO();
        }
        int pageNum = userQueryDTO.getPageNum() == null ? 1 : userQueryDTO.getPageNum();
        int pageSize = userQueryDTO.getPageSize() == null ? 10 : userQueryDTO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询总数和列表（匹配UserMapper.countByAllParam和selectByAllParam）
        userQueryDTO.setOffset(offset);
        long total = userMapper.countByQuery(userQueryDTO);
        List<User> userList = userMapper.selectByQuery(userQueryDTO);

        // 3. 转换为UserDetailDTO列表
        List<UserDetailDTO> dtoList = userList.stream()
                .map(userConvert::userToUserDetailDTO)
                .collect(Collectors.toList());

        // 4. 封装PageResult（匹配PageResult结构）
        long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return new PageResult<UserDetailDTO>(total, totalPages, dtoList, pageNum, pageSize);
    }

    /**
     * 更新用户角色
     */
    @Override
    public Boolean updateUserRole(Long userId, UserRoleEnum role) {
        if (userId == null || role == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        selectUserById(userId);
        int rows = userMapper.updateUserRole(role, userId);

        if (rows <= 0) {
            log.error("更新用户角色失败，用户ID：{}，角色：{}", userId, role);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        log.info("更新用户角色成功，用户ID：{}，角色：{}", userId, role);
        return rows > 0;
    }

    /**
     * 更新用户状态
     */
    @Override
    public Boolean updateUserStatus(Long userId, UserStatusEnum status) {
        if (userId == null || status == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        selectUserById(userId);
        int rows = userMapper.updateUserStatus(userId, status);

        if (rows <= 0) {
            log.error("更新用户状态失败，用户ID：{}，状态：{}", userId, status);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }
        log.info("更新用户状态成功，用户ID：{}，状态：{}", userId, status);

        return rows > 0;
    }

    /**
     * 新增：密码校验方法（使用Spring Security的匹配器）
     * 用于登录时验证密码正确性
     */
    @Override
    public Boolean verifyPassword(Long userId, String rawPassword) {
        // 1.获取用户信息
        User user = getById(userId);

        // 2.使用加密器的matches方法验证原始密码与加密密码是否匹配
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * 验证用户角色（通过Token）
     */
    @Override
    public Boolean verifyRole(Long userId, UserRoleEnum role) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        return user.getRole().equals(role);
    }


    // ---------------------- 私有辅助方法（匹配文档转换规则） ----------------------
    /**
     * 校验注册参数（严格匹配RegisterDTO校验规则）
     */
    private void validateRegisterParam(RegisterDTO registerDTO) {
        if (!StringUtils.hasText(registerDTO.getUsername()) || registerDTO.getUsername().length() > 20) {
            throw new BusinessException(ErrorCode.USERNAME_FORMAT_INVALID);
        }
        if (StringUtils.hasText(registerDTO.getPhoneNumber())
                && !registerDTO.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.PHONE_FORMAT_INVALID);
        }
        if (StringUtils.hasText(registerDTO.getEmail())
                && !registerDTO.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_INVALID);
        }
        if (!registerDTO.getPassword().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$")) {
            throw new BusinessException(ErrorCode.PASSWORD_FORMAT_INVALID);
        }
    }

    /**
     * 第三方用户自动注册
     */
    private User autoRegisterThirdPartyUser(ThirdPartyTypeEnum thirdType, String openid) {
        User user = new User();
        user.setUsername(thirdType.name() + "_" + openid.substring(openid.length() - 4));
        user.setPassword(passwordEncoder.encode(openid + System.currentTimeMillis()));
        user.setCreditScore(INIT_CREDIT_SCORE);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        return userMapper.selectById(user.getUserId());
    }

    /**
     * 绑定第三方账号（内部调用）
     */
    private void bindThirdPartyAccount(ThirdPartyTypeEnum thirdType, Long userId, String openid, String accessToken) {
        UserThirdParty userThirdParty = new UserThirdParty();
        userThirdParty.setUserId(userId);
        userThirdParty.setThirdType(thirdType);
        userThirdParty.setOpenid(openid);
        userThirdParty.setAccessToken(accessToken);
        userThirdParty.setBindTime(LocalDateTime.now());
        userThirdParty.setIsValid(1);
        userThirdPartyMapper.insert(userThirdParty);
    }

    /**
     * 密码登录校验
     */
    private void validatePassword(User user, String rawPassword) {
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
    }
}
