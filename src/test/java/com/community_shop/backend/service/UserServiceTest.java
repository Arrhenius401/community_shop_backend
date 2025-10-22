package com.community_shop.backend.service;

import com.community_shop.backend.convert.UserConvert;
import com.community_shop.backend.dto.user.LoginDTO;
import com.community_shop.backend.dto.user.LoginResultDTO;
import com.community_shop.backend.dto.user.RegisterDTO;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.enums.SimpleEnum.LoginTypeEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.impl.UserServiceImpl;
import com.community_shop.backend.utils.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserThirdPartyMapper userThirdPartyMapper;

    @Mock
    private UserConvert userConvert;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenUtil tokenUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterDTO testRegisterDTO;
    private LoginDTO testLoginDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setStatus(UserStatusEnum.NORMAL);
        testUser.setRole(UserRoleEnum.USER);

        // 初始化注册DTO
        testRegisterDTO = new RegisterDTO();
        testRegisterDTO.setUsername("newUser");
        testRegisterDTO.setPassword("Password123");
        testRegisterDTO.setPhoneNumber("13800138000");
        testRegisterDTO.setEmail("test@example.com");
        testRegisterDTO.setVerifyCode("123456");

        // 初始化登录DTO
        testLoginDTO = new LoginDTO();
        testLoginDTO.setLoginType(LoginTypeEnum.EMAIL);
        testLoginDTO.setLoginId("test@example.com");
        testLoginDTO.setCredential("Password123");

        // 关键修复：通过反射给父类的 baseMapper 字段赋值
        // 因为UserServiceImpl继承了BaseServiceImpl，需要手动注入mapper
        try {
            // 注意：这里改为 ServiceImpl.class（MyBatis-Plus 提供的父类）
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            // 给 userService 注入 mock 的 userMapper
            baseMapperField.set(userService, userMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化 baseMapper 失败", e);
        }

        // 模拟RedisTemplate的opsForValue()返回ValueOperations对象
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 模拟 set 方法成功执行（无返回值，用 doNothing()）
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testRegister_Success() {
        // 模拟 DTO 转实体的方法
        when(userConvert.registerDtoToUser(any(RegisterDTO.class))).thenAnswer(invocation -> {
            RegisterDTO dto = invocation.getArgument(0);
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(dto.getPassword());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setEmail(dto.getEmail());
            // 设置其他必要字段（如状态、角色等）
            user.setStatus(UserStatusEnum.NORMAL);
            user.setRole(UserRoleEnum.USER);
            return user;
        });
        // 模拟依赖行为
        when(userMapper.selectByPhone(anyString())).thenReturn(null);
        when(userMapper.selectByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        Boolean result = userService.register(testRegisterDTO);

        // 验证结果
        assertTrue(result);
        verify(userMapper, times(1)).insert(any(User.class));
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                anyString(),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
    }

    @Test
    void testRegister_PhoneExists() {
        // 模拟手机号已存在
        when(userMapper.selectByPhone(anyString())).thenReturn(testUser);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.register(testRegisterDTO);
        }, "应抛出手机号已存在异常");
    }

    @Test
    void testLogin_Success() {
        // 模拟依赖行为
        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenUtil.generateToken(anyLong())).thenReturn("testToken");
        when(tokenUtil.getExpirationTimeFromToken(anyString())).thenReturn(LocalDateTime.now().plusHours(1));

        // 执行测试
        LoginResultDTO result = userService.login(testLoginDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(testUser.getUserId(), result.getUserInfo().getUserId());
    }

    @Test
    void testLogin_PasswordError() {
        // 模拟密码错误
        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.login(testLoginDTO);
        }, "应抛出密码错误异常");
    }

    @Test
    void testVerifyPassword_Success() {
        // 模拟依赖行为
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // 执行测试
        Boolean result = userService.verifyPassword(1L, "rawPassword");

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUpdateCreditScore_Success() {

        // 模拟依赖行为
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userMapper.updateCreditScore(anyLong(), anyInt())).thenReturn(1);

        // 执行测试
        Boolean result = userService.updateCreditScore(1L, 10, "测试加分");

        // 验证结果
        assertTrue(result);
        // 显式验证 Redis 调用
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                anyString(),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
    }

    @Test
    void testUpdateUserRole_Success() {
        // 模拟依赖行为
        when(userMapper.selectById(1L)).thenReturn(testUser); // 操作者
        when(userMapper.selectById(2L)).thenReturn(new User()); // 目标用户
        when(userMapper.updateUserRole(any(UserRoleEnum.class), anyLong())).thenReturn(1);

        // 执行测试
        Boolean result = userService.updateUserRole(1L, 2L, UserRoleEnum.ADMIN);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUpdateUserRole_NoPermission() {
        // 模拟非管理员操作
        User normalUser = new User();
        normalUser.setUserId(1L);
        normalUser.setRole(UserRoleEnum.USER);
        when(userMapper.selectById(1L)).thenReturn(normalUser);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.updateUserRole(1L, 2L, UserRoleEnum.ADMIN);
        }, "应抛出权限不足异常");
    }
}