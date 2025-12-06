package com.community_shop.backend.service;

import com.community_shop.backend.convert.UserConvert;
import com.community_shop.backend.dto.user.*;
import com.community_shop.backend.dto.verification.VerifyEmailDTO;
import com.community_shop.backend.dto.verification.VerifyPhoneDTO;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.code.UserRoleEnum;
import com.community_shop.backend.enums.code.UserStatusEnum;
import com.community_shop.backend.enums.simple.LoginTypeEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.dao.mapper.UserMapper;
import com.community_shop.backend.dao.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.impl.EmailCodeServiceImpl;
import com.community_shop.backend.service.impl.PhoneCodeServiceImpl;
import com.community_shop.backend.service.impl.UserServiceImpl;
import com.community_shop.backend.utils.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Duration;
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
    private EmailCodeServiceImpl emailCodeService;

    @Mock
    private PhoneCodeServiceImpl phoneCodeService;

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
        testUser.setRole(UserRoleEnum.ADMIN);

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
        // 放宽对 Redis 相关 stub 的严格性
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 模拟 set 方法成功执行（无返回值，用 doNothing()）
        lenient().doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
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
        doReturn( null).when(userMapper).selectByPhone(anyString());
        doReturn( null).when(userMapper).selectByEmail(anyString());
        doReturn("encodePassword").when(passwordEncoder).encode(anyString());
        doReturn(1).when(userMapper).insert(any(User.class));

        // 执行测试
        Boolean result = userService.register(testRegisterDTO);

        // 验证结果
        assertTrue(result);
        verify(userMapper, times(1)).insert(any(User.class));
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                anyString(),
                any(),
                any(Duration.class)
        );
    }

    @Test
    void testRegister_PhoneExists() {
        // 模拟手机号已存在
        doReturn(testUser).when(userMapper).selectByPhone(anyString());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.register(testRegisterDTO);
        }, "应抛出手机号已存在异常");
    }

    @Test
    void testRegisterByEmail_Success() {
        // 初始化测试数据
        RegisterEmailDTO dto = new RegisterEmailDTO();
        dto.setEmail("new@example.com");
        dto.setPassword("Password123");
        dto.setVerifyCode("123456");

        // 模拟依赖行为
        when(userMapper.selectByEmail(anyString())).thenReturn(null);
        when(emailCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userConvert.registerEmailDtoToUser(any(RegisterEmailDTO.class))).thenAnswer(invocation -> {
            RegisterEmailDTO param = invocation.getArgument(0);
            User user = new User();
            user.setEmail(param.getEmail());
            user.setUsername(param.getEmail().split("@")[0]);
            return user;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        Boolean result = userService.registerByEmail(dto);

        // 验证结果
        assertTrue(result);
        verify(userMapper, times(1)).insert(any(User.class));
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    void testRegisterByEmail_EmailExists() {
        RegisterEmailDTO dto = new RegisterEmailDTO();
        dto.setEmail("existing@example.com");

        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
    }

    @Test
    void testRegisterByEmail_VerifyCodeError() {
        RegisterEmailDTO dto = new RegisterEmailDTO();
        dto.setEmail("new@example.com");
        dto.setVerifyCode("wrong");

        when(userMapper.selectByEmail(anyString())).thenReturn(null);
        when(emailCodeService.verifyCode(anyString(), anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
    }

    @Test
    void testRegisterByPhone_Success() {
        RegisterPhoneDTO dto = new RegisterPhoneDTO();
        dto.setPhoneNumber("13900139000");
        dto.setPassword("Password123");
        dto.setVerifyCode("123456");

        when(userMapper.selectByPhone(anyString())).thenReturn(null);
        when(phoneCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userConvert.registerPhoneDtoToUser(any(RegisterPhoneDTO.class))).thenAnswer(invocation -> {
            RegisterPhoneDTO param = invocation.getArgument(0);
            User user = new User();
            user.setPhoneNumber(param.getPhoneNumber());
            user.setUsername("user_" + param.getPhoneNumber().substring(7));
            return user;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        Boolean result = userService.registerByPhone(dto);
        assertTrue(result);
    }

    @Test
    void testRegisterByPhone_PhoneExists() {
        RegisterPhoneDTO dto = new RegisterPhoneDTO();
        dto.setPhoneNumber("13800138000");

        when(userMapper.selectByPhone(anyString())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
    }

    @Test
    void testBindEmail_Success() {
        VerifyEmailDTO dto = new VerifyEmailDTO();
        dto.setUserId(1L);
        dto.setEmail("bind@example.com");
        dto.setVerifyCode("123456");

        when(userMapper.selectByEmail(anyString())).thenReturn(null);
        when(emailCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.updateEmail(anyLong(), anyString())).thenReturn(1);

        Boolean result = userService.bindEmail(dto);
        assertTrue(result);
    }

    @Test
    void testBindEmail_EmailExists() {
        VerifyEmailDTO dto = new VerifyEmailDTO();
        dto.setEmail("existing@example.com");

        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.bindEmail(dto));
    }

    @Test
    void testBindPhone_Success() {
        VerifyPhoneDTO dto = new VerifyPhoneDTO();
        dto.setUserId(1L);
        dto.setPhoneNumber("13900139000");
        dto.setVerifyCode("123456");

        when(userMapper.selectByPhone(anyString())).thenReturn(null);
        when(phoneCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.updatePhoneNumber(anyLong(), anyString())).thenReturn(1);

        Boolean result = userService.bindPhone(dto);
        assertTrue(result);
    }

    @Test
    void testUpdatePasswordByEmail_Success() {
        PasswordUpdateEmailDTO dto = new PasswordUpdateEmailDTO();
        dto.setUserId(1L);
        dto.setEmail("test@example.com");
        dto.setVerifyCode("123456");
        dto.setNewPassword("NewPassword123");

        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);
        when(emailCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.updatePassword(anyLong(), anyString())).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        Boolean result = userService.updatePasswordByEmail(dto);
        assertTrue(result);
    }

    @Test
    void testUpdatePasswordByEmail_NotBelong() {
        PasswordUpdateEmailDTO dto = new PasswordUpdateEmailDTO();
        dto.setUserId(2L); // 与测试用户ID不一致
        dto.setEmail("test@example.com");
        dto.setNewPassword("NewPassword123");

        when(userMapper.selectByEmail(anyString())).thenReturn(testUser);

        assertThrows(BusinessException.class, () ->
                userService.updatePasswordByEmail(dto));
    }

    @Test
    void testUpdatePasswordByPhone_Success() {
        PasswordUpdatePhoneDTO dto = new PasswordUpdatePhoneDTO();
        dto.setUserId(1L);
        dto.setPhoneNumber("13800138000");
        dto.setVerifyCode("123456");
        dto.setNewPassword("NewPassword123");

        when(userMapper.selectByPhone(anyString())).thenReturn(testUser);
        when(phoneCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.updatePassword(anyLong(), anyString())).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        Boolean result = userService.updatePasswordByPhone(dto);
        assertTrue(result);
    }

    @Test
    void testUpdatePasswordByPhone_NotBelong() {
        PasswordUpdatePhoneDTO dto = new PasswordUpdatePhoneDTO();
        dto.setUserId(2L); // 与测试用户ID不一致
        dto.setPhoneNumber("13800138000");
        dto.setNewPassword("NewPassword123");

        when(userMapper.selectByPhone(anyString())).thenReturn(testUser);

        assertThrows(BusinessException.class, () ->
                userService.updatePasswordByPhone(dto));
    }

    @Test
    void testLogin_Success() {
        // 模拟依赖行为
        doReturn(testUser).when(userMapper).selectByEmail(anyString());
        doReturn( true).when(passwordEncoder).matches(anyString(), anyString());
        doReturn("testToken").when(tokenUtil).generateToken(anyLong());
        doReturn(LocalDateTime.now().plusHours(1)).when(tokenUtil).getExpirationTimeFromToken(anyString());
        when(userConvert.userToLoginResultUserSimpleDTO(any(User.class))).thenAnswer(invocation -> {
            LoginResultDTO.UserSimpleDTO user = new LoginResultDTO.UserSimpleDTO();
            user.setUserId(testUser.getUserId());
            user.setUsername(testUser.getUsername());
            user.setAvatarUrl(testUser.getAvatarUrl());
            user.setCreditScore(testUser.getCreditScore());
            user.setIsAdmin(testUser.isAdmin());
            return user;
        });

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
        doReturn(testUser).when(userMapper).selectByEmail(anyString());
        doReturn( false).when(passwordEncoder).matches(anyString(), anyString());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.login(testLoginDTO);
        }, "应抛出密码错误异常");
    }

    @Test
    void testVerifyPassword_Success() {
        // 模拟依赖行为
        // 重点：直接存根 Service 层的 getById 方法，而非 Mapper 层
        doReturn(testUser).when(userMapper).selectById(1L);
        // 密码匹配器的存根保持不变
        doReturn(true).when(passwordEncoder).matches(anyString(), eq(testUser.getPassword()));

        // 执行测试
        Boolean result = userService.verifyPassword(1L, "rawPassword");

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUpdateCreditScore_Success() {

        // 模拟依赖行为
        doReturn(testUser).when(userMapper).selectById(2L);
        doReturn(1).when(userMapper).updateCreditScore(anyLong(), anyInt());

        // 执行测试
        Boolean result = userService.updateCreditScore(2L, 10, "测试加分");

        // 验证结果
        assertTrue(result);
        // 显式验证 Redis 调用
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                anyString(),
                any(),
                any(Duration.class)
        );
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testUpdateUserRole_Success() {
        // 模拟被修改的用户
        User targetUser = new User();
        targetUser.setRole(UserRoleEnum.USER);

        // 模拟依赖行为
        doReturn(testUser).when(userMapper).selectById(3L); // 操作者
        doReturn(targetUser).when(userMapper).selectById(4L); // 目标用户
        doReturn(1).when(userMapper).updateUserRole(any(UserRoleEnum.class), eq(4L));

        // 执行测试
        Boolean result = userService.updateUserRole(3L, 4L, UserRoleEnum.ADMIN);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUpdateUserRole_NoPermission() {
        // 模拟非管理员操作
        User normalUser = new User();
        normalUser.setUserId(1L);  // 操作者ID是1L
        normalUser.setRole(UserRoleEnum.USER);
        // 修正：为操作者ID(1L)添加stub
        doReturn(normalUser).when(userMapper).selectById(1L);

        // 目标用户ID是5L
        // 但在代码执行过程中，会在 verifyRole() 处爆出异常,不会去查询目标用户（5L）的信息
        // 因此 selectById(5L) 的 stub 定义了但未被使用，成为不必要的 stub
//        User targetUser = new User();
//        targetUser.setUserId(5L);
//        doReturn(targetUser).when(userMapper).selectById(5L);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.updateUserRole(1L, 5L, UserRoleEnum.ADMIN);
        }, "应抛出权限不足异常");
    }
}