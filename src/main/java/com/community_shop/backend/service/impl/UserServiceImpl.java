package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.user.LoginDTO;
import com.community_shop.backend.enums.SimpleEnum.LoginTypeEnum;
import com.community_shop.backend.dto.user.RegisterDTO;
import com.community_shop.backend.dto.user.UserProfileUpdateDTO;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Objects;

// "object == null" 与 “Objects.isNull(object)” 等价
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    // 信用分常量
    private static final Integer INIT_CREDIT_SCORE = 100;
    private static final Integer MIN_CREDIT_SCORE = 0;

    // 缓存相关常量
    private static final String CACHE_KEY_USER = "user:info:"; // 用户信息缓存Key前缀
    private static final Duration CACHE_TTL_USER = Duration.ofHours(1); // 用户信息缓存1小时

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

    /**
     * 新增用户（内部调用，注册流程前置）
     * 密码加密存储，初始化信用分100
     */
    @Override
    public Integer insertUser(User user) {
        try {
            // 1. 密码BCrypt加密
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);

            // 2. 初始化用户基础数据
            user.setCreditScore(INIT_CREDIT_SCORE); // 基础信用分100
            user.setPostCount(0);
            user.setFollowerCount(0);

            // 3. 插入数据库
            Integer insertRows = userMapper.insert(user);
            if (insertRows <= 0) {
                log.error("插入用户失败，用户信息：{}", user);
                throw new BusinessException(ErrorCode.FAILURE);
            }
            return insertRows;
        } catch (BusinessException e) {
            throw e; // 抛出业务异常，由全局处理器处理
        } catch (Exception e) {
            log.error("插入用户异常", e);
            throw new BusinessException(ErrorCode.FAILURE);
        }

    }

    /**
     * 根据用户ID查询详情（密码脱敏）
     */
    @Override
    public User selectUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 1. 先查缓存
        User user = (User) redisTemplate.opsForValue().get(CACHE_KEY_USER + userId);
        if (Objects.nonNull(user)) {
            // 脱敏处理（隐藏密码）
            user.setPassword(null);
            return user;
        }

        // 2. 缓存未命中，查数据库
        user = userMapper.selectById(userId);
        if (user == null) {
            return user;
        }

        // 3. 脱敏并缓存
        user.setPassword(null);
        redisTemplate.opsForValue().set(CACHE_KEY_USER + userId, user, CACHE_TTL_USER);
        return user;
    }

    /**
     * 根据手机号查询用户（用于登录校验）
     */
    @Override
    public User selectUserByPhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        User user = userMapper.selectByPhone(phoneNumber);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        user.setPassword(null); // 脱敏处理
        return user;
    }

    /**
     * 更新用户资料（仅允许修改非敏感字段）
     */
    @Override
    public Boolean updateUserProfile(Long userId, UserProfileUpdateDTO profileVO) {
        if (userId == null || profileVO == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 校验用户存在
        selectUserById(userId); // 直接调用查询方法，不存在会自动抛异常

        // 构建更新实体
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setUsername(profileVO.getUsername());
        updateUser.setAvatarUrl(profileVO.getAvatarUrl());
        updateUser.setInterestTags(profileVO.getInterestTags());

        int rows = userMapper.updateById(updateUser);
        if (rows <= 0) {
            log.error("更新用户资料失败，用户ID：{}，资料：{}", userId, profileVO);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        // 更新缓存
        redisTemplate.delete(CACHE_KEY_USER + userId);
        log.info("更新用户资料成功，用户ID：{}", userId);
        return rows > 0;
    }

    /**
     * 注销账号（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 校验用户存在
        selectUserById(userId);

        int rows = userMapper.deleteById(userId);
        if (rows <= 0) {
            log.error("删除用户失败，用户ID：{}", userId);
            throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
        }

        // 清除缓存
        redisTemplate.delete(CACHE_KEY_USER + userId);
        log.info("删除用户成功，用户ID：{}", userId);
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
     * 更新用户密码
     */
    @Override
    public Boolean updateUserPassword(Long userId, String newPassword) {
        if (userId == null || !StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        selectUserById(userId);
        int rows = userMapper.updateUserPassword(passwordEncoder.encode(newPassword), userId);

        if (rows <= 0) {
            log.error("更新用户密码失败，用户ID：{}，新密码：{}", userId, newPassword);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        log.info("更新用户密码成功，用户ID：{}，新密码：{}", userId, newPassword);

        return rows > 0;
    }

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
     * 用户注册（手机号+验证码模式）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(RegisterDTO registerDTO) {
        // 1. 参数校验（保持不变）
        if (registerDTO == null || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 校验手机号/邮箱是否已注册（保持不变）
        if (StringUtils.hasText(registerDTO.getPhoneNumber())) {
            User phoneUser = userMapper.selectByPhone(registerDTO.getPhoneNumber());
            if (phoneUser != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        } else if (StringUtils.hasText(registerDTO.getEmail())) {
            User emailUser = userMapper.selectByEmail(registerDTO.getEmail());
            if (emailUser != null) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        } else {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

//        // 3. 验证码校验（此处简化，实际需对接短信服务）
//        if (!"123456".equals(registerDTO.getVerifyCode())) { // 示例验证码，实际需动态生成
//            throw new BusinessException(ErrorCodeEnum.VERIFY_CODE_ERROR);
//        }

        // 3. 插入用户数据
        User user = new User();
        user.setPhoneNumber(registerDTO.getPhoneNumber());
        user.setPassword(registerDTO.getPassword());
        user.setUsername(registerDTO.getUsername()); // 生成默认用户名
        insertUser(user);

        // 4. 生成登录Token
        return tokenUtil.generateToken( "USER", user.getUserId().toString());
    }

    /**
     * 用户登录（登录凭证+密码模式）
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String login(LoginDTO loginDTO) {
        // 1. 参数校验（保持不变）
        if(loginDTO == null || !StringUtils.hasText(loginDTO.getCredential())
                || loginDTO.getLoginType() == null || loginDTO.getLoginId() == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        try {
            // 2. 根据登录凭证查询用户
            LoginTypeEnum loginType = loginDTO.getLoginType();
            User user = null;
            switch (loginType){
                case EMAIL:
                    user = userMapper.selectByEmail(loginDTO.getLoginId());
                    if (user == null) {
                        throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
                    }
                    if (!verifyPassword(loginDTO.getCredential(), user.getPassword())) {
                        throw new BusinessException(ErrorCode.PASSWORD_ERROR);
                    }
                    break;

                case PHONE_NUMBER:
                    user = userMapper.selectByPhone(loginDTO.getLoginId());
                    if (user == null) {
                        throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
                    }
                    if (!verifyPassword(loginDTO.getCredential(), user.getPassword())) {
                        throw new BusinessException(ErrorCode.PASSWORD_ERROR);
                    }
                    break;
            }

            // 3. 校验用户存在
            if (user == null){
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            return tokenUtil.generateToken("USER", user.getUserId().toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.FAILURE);
        }
    }

    /**
     * 第三方登录（微信/QQ等）
     */
    // 指定事务的隔离级别为 READ_COMMITTED（读已提交）
    // 指定哪些异常发生时，事务需要「回滚」（即撤销方法中已执行的数据库操作）
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String loginByThirdParty(ThirdPartyTypeEnum platform, String code) {
        if (platform == null || !StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        try {
            // 获取第三方OpenID
            String openId = thirdPartyAuthUtil.getOpenId(platform, code);
            if (!StringUtils.hasText(openId)) {
                throw new BusinessException(ErrorCode.THIRD_AUTH_FAILED);
            }

            // 查询OpenID是否已绑定
            UserThirdParty bindingRecord = userThirdPartyMapper.selectByThirdTypeAndOpenid(platform, openId);
            if (bindingRecord != null || bindingRecord.getIsValid() == 0) {
                return tokenUtil.generateToken("USER", bindingRecord.getUserId().toString());
            }

            // 未绑定，自动注册用户
            User newUser = new User();
            newUser.setUsername(platform + "_" + openId.substring(0, 8));
            newUser.setPassword(passwordEncoder.encode(openId));
            newUser.setCreditScore(INIT_CREDIT_SCORE);

            int insertRows = userMapper.insert(newUser);
            if (insertRows <= 0) {
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 绑定第三方账号（假设实现）
            // userThirdPartyMapper.insert(newUser.getUserId(), platform, openId);

            // 缓存用户并生成Token
            redisTemplate.opsForValue().set(CACHE_KEY_USER + newUser.getUserId(), newUser, CACHE_TTL_USER);
            log.info("第三方登录自动注册成功，用户ID：{}，平台：{}", newUser.getUserId(), platform);
            return tokenUtil.generateToken("USER",  newUser.getUserId().toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("第三方登录系统异常", e);
            throw new BusinessException(ErrorCode.THIRD_SYSTEM_ERROR);
        }
    }

    /**
     * 更新用户信用分（支持增减，最低为0）
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Integer updateCreditScore(Long userId, Integer scoreChange, String reason) {
        if (userId == null || scoreChange == null || !StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 获取当前用户
        User user = selectUserById(userId);
        int currentScore = user.getCreditScore();

        // 计算新信用分
        int newCreditScore = currentScore + scoreChange;
        newCreditScore = Math.max(newCreditScore, MIN_CREDIT_SCORE);

        // 更新信用分
        int rows = userMapper.updateCreditScore(userId, newCreditScore);
        if (rows <= 0) {
            log.error("更新信用分失败，用户ID：{}，变更值：{}，原因：{}", userId, scoreChange, reason);
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        // 更新缓存
        user.setCreditScore(newCreditScore);
        redisTemplate.opsForValue().set(CACHE_KEY_USER + userId, user, CACHE_TTL_USER);
        log.info("更新信用分成功，用户ID：{}，原分数：{}，变更值：{}，新分数：{}，原因：{}",
                userId, currentScore, scoreChange, newCreditScore, reason);
        return newCreditScore;
    }

    /**
     * 新增：密码校验方法（使用Spring Security的匹配器）
     * 用于登录时验证密码正确性
     */
    @Override
    public Boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 使用加密器的matches方法验证原始密码与加密密码是否匹配
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证用户角色（通过Token）
     */
    @Override
    public Boolean verifyRole(Long userId, UserRoleEnum role) {
        User user = selectUserById(userId);
        if (user == null) {
            return false;
        }
        return user.getRole().equals(role);
    }
}
