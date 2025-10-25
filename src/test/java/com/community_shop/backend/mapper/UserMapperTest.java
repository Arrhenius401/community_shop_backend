package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.SimpleEnum.GenderEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.CodeEnum.UserStatusEnum;
import com.community_shop.backend.dto.user.UserQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.1节 UserMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.8节 User实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 */
@MybatisTest  // 仅加载MyBatis相关Bean，测试更轻量
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（application-test.properties）
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;  // 注入待测试的UserMapper

    // 测试复用的基础数据（从data-common.sql初始化的用户中获取）
    private User testBuyer;    // 普通买家（userId=1，role=USER，status=NORMAL）
    private User testSeller;   // 卖家（userId=2，role=USER，status=NORMAL）
    private User testAdmin;    // 管理员（userId=3，role=ADMIN，status=NORMAL）
    private User testBanned;   // 封禁用户（userId=4，role=USER，status=BANNED）

    /**
     * 测试前初始化：从数据库查询基础测试用户，确保与data-common.sql数据一致
     * 适配《代码文档0》中User实体的枚举属性（role/status/gender）
     */
    @BeforeEach
    void setUp() {
        // 按用户名查询（《代码文档1》2.1.2节 selectByUsername方法）
        testBuyer = userMapper.selectByUsername("test_buyer");
        testSeller = userMapper.selectByUsername("test_seller");
        testAdmin = userMapper.selectByUsername("test_admin");
        testBanned = userMapper.selectByUsername("test_banned");

        // 断言初始化成功（确保SQL脚本已正确执行）
        assertNotNull(testBuyer, "初始化失败：未查询到test_buyer（data-common.sql中用户）");
        assertNotNull(testSeller, "初始化失败：未查询到test_seller（data-common.sql中用户）");
        assertNotNull(testAdmin, "初始化失败：未查询到test_admin（data-common.sql中用户）");
        assertNotNull(testBanned, "初始化失败：未查询到test_banned（data-common.sql中用户）");
    }

    /**
     * 测试selectByUsername：存在用户（正常场景）
     * 适配《代码文档1》2.1.2节 登录与身份校验 - selectByUsername方法
     */
    @Test
    void selectByUsername_existUser_returnsUser() {
        // 执行测试方法（查询test_buyer）
        User result = userMapper.selectByUsername("test_buyer");

        // 断言结果（匹配《代码文档0》User实体属性与枚举值）
        assertNotNull(result);
        assertEquals(testBuyer.getUserId(), result.getUserId());
        assertEquals("test_buyer", result.getUsername());
        assertEquals("buyer@test.com", result.getEmail());
        assertEquals("13800138001", result.getPhoneNumber());
        assertEquals(UserRoleEnum.USER, result.getRole());  // 枚举自动匹配（《中间件文档3》）
        assertEquals(UserStatusEnum.NORMAL, result.getStatus());
        assertEquals(GenderEnum.MALE, result.getGender());
    }

    /**
     * 测试selectByUsername：不存在用户（异常场景）
     */
    @Test
    void selectByUsername_nonExistUser_returnsNull() {
        User result = userMapper.selectByUsername("non_exist_user");
        assertNull(result, "查询不存在的用户应返回null");
    }

    /**
     * 测试selectByPhone：存在手机号（正常场景）
     * 适配《代码文档1》2.1.2节 登录与身份校验 - selectByPhone方法
     */
    @Test
    void selectByPhone_existPhone_returnsUser() {
        // 执行测试方法（查询test_seller的手机号）
        User result = userMapper.selectByPhone("13900139001");

        assertNotNull(result);
        assertEquals(testSeller.getUserId(), result.getUserId());
        assertEquals("test_seller", result.getUsername());
    }

    /**
     * 测试selectByEmail：存在邮箱（正常场景）
     * 适配《代码文档1》2.1.2节 登录与身份校验 - selectByEmail方法
     */
    @Test
    void selectByEmail_existEmail_returnsUser() {
        // 执行测试方法（查询test_admin的邮箱）
        User result = userMapper.selectByEmail("admin@test.com");

        assertNotNull(result);
        assertEquals(testAdmin.getUserId(), result.getUserId());
        assertEquals(UserRoleEnum.ADMIN, result.getRole());  // 管理员角色验证
    }

    /**
     * 测试updatePassword：更新密码（正常场景）
     * 适配《代码文档1》2.1.2节 登录与身份校验 - updatePassword方法
     */
    @Test
    void updatePassword_validParam_returnsAffectedRows1() {
        // 1. 准备参数（test_buyer的userId，新密码BCrypt加密后的值，原始密码：654321）
        Long userId = testBuyer.getUserId();
        String newEncryptedPwd = "$2a$10$EixZaYb051F7EUvBa4sn5e965g3MfF1BuVx5F3k8WX2vF5G5H5H6";

        // 2. 执行更新方法
        int affectedRows = userMapper.updatePassword(userId, newEncryptedPwd);

        // 3. 断言更新行数（预期1行）
        assertEquals(1, affectedRows, "更新密码应影响1行数据");

        // 4. 重新查询用户，验证密码已更新
        User updatedUser = userMapper.selectById(userId);
        assertEquals(newEncryptedPwd, updatedUser.getPassword(), "密码更新失败");
    }

    /**
     * 测试updateCreditScore：更新信用分（正常场景，加分）
     * 适配《代码文档1》2.1.2节 信用与统计数据更新 - updateCreditScore方法
     */
    @Test
    void updateCreditScore_addScore_returnsAffectedRows1() {
        // 1. 准备参数（test_seller当前信用分90，更新为100）
        Long userId = testSeller.getUserId();
        Integer newScore = 100;

        // 2. 执行更新方法
        int affectedRows = userMapper.updateCreditScore(userId, newScore);

        // 3. 断言更新行数
        assertEquals(1, affectedRows);

        // 4. 验证信用分已更新
        User updatedUser = userMapper.selectById(userId);
        assertEquals(newScore, updatedUser.getCreditScore(), "信用分更新失败");
    }

    /**
     * 测试updateUserStatus：封禁用户（枚举参数，正常场景）
     * 适配《代码文档1》2.1.2节 信用与统计数据更新 - updateUserStatus方法
     * 适配《中间件文档3》枚举TypeHandler自动转换（枚举→数据库code）
     */
    @Test
    void updateUserStatus_banUser_returnsAffectedRows1() {
        // 1. 准备参数（将test_buyer的状态从NORMAL改为BANNED）
        Long userId = testBuyer.getUserId();
        UserStatusEnum newStatus = UserStatusEnum.BANNED;

        // 2. 执行更新方法（直接传递枚举对象，TypeHandler自动转换为"CODE"）
        int affectedRows = userMapper.updateUserStatus(userId, newStatus);

        // 3. 断言更新行数
        assertEquals(1, affectedRows);

        // 4. 验证状态已更新（查询结果自动转换为枚举）
        User updatedUser = userMapper.selectById(userId);
        assertEquals(newStatus, updatedUser.getStatus(), "用户状态更新失败");
    }

    /**
     * 测试updateUserRole：提升角色为管理员（枚举参数，正常场景）
     * 适配《代码文档1》2.1.2节 信用与统计数据更新 - updateUserRole方法
     */
    @Test
    void updateUserRole_promoteAdmin_returnsAffectedRows1() {
        // 1. 准备参数（将test_seller的角色从USER改为ADMIN）
        UserRoleEnum newRole = UserRoleEnum.ADMIN;
        Long userId = testSeller.getUserId();

        // 2. 执行更新方法
        int affectedRows = userMapper.updateUserRole(newRole, userId);

        // 3. 断言更新行数
        assertEquals(1, affectedRows);

        // 4. 验证角色已更新
        User updatedUser = userMapper.selectById(userId);
        assertEquals(newRole, updatedUser.getRole(), "用户角色更新失败");
    }

    /**
     * 测试selectByRole：查询普通用户（枚举参数，正常场景）
     * 适配《代码文档1》2.1.2节 条件查询与统计 - selectByRole方法
     */
    @Test
    void selectByRole_queryUserRole_returnsUserList() {
        // 1. 执行查询方法（查询角色为USER的所有用户）
        List<User> userList = userMapper.selectByRole(UserRoleEnum.USER);

        // 2. 断言结果（data-common.sql中USER角色有3人：test_buyer/test_seller/test_banned）
        assertNotNull(userList);
        assertEquals(3, userList.size(), "查询普通用户数量错误");
        // 验证列表中无ADMIN角色用户
        boolean hasAdmin = userList.stream().anyMatch(u -> UserRoleEnum.ADMIN.equals(u.getRole()));
        assertFalse(hasAdmin, "普通用户查询不应包含管理员");
    }

    /**
     * 测试getUsersByAllParam：多条件组合查询（筛选+排序+分页）
     * 适配《代码文档1》2.1.2节 条件查询与统计 - getUsersByAllParam方法
     */
    @Test
    void getUsersByAllParam_multiCondition_returnsPagedList() {
        // 1. 准备参数（筛选字段：credit_score≥80；排序字段：create_time；排序方向：DESC；分页：offset=0，limit=2）
        String compareIndex = "credit_score";  // 筛选字段（表字段名，下划线命名）
        String compareParam = "80";            // 筛选值（信用分≥80）
        String order = "create_time";          // 排序字段（表字段名）
        String direction = "DESC";             // 排序方向
        int limit = 2;                         // 每页条数
        int offset = 0;                        // 分页偏移量（第1页）

        // 2. 执行查询方法
        List<User> userList = userMapper.getUsersByAllParam(
                compareIndex, compareParam, order, direction, limit, offset
        );

        // 3. 断言结果（符合条件的用户：test_buyer(100)/test_admin(100)/test_seller(90)，按create_time降序取前2）
        assertNotNull(userList);
        assertEquals(2, userList.size());
        // 验证排序：test_buyer（10:00）→ test_admin（08:00）（降序）
        assertEquals("test_buyer", userList.get(0).getUsername());
        assertEquals("test_admin", userList.get(1).getUsername());
    }

    /**
     * 测试selectByQuery：复杂条件查询（DTO参数，正常场景）
     * 适配《代码文档1》2.1.2节 条件查询与统计 - selectByQuery方法
     */
    @Test
    void selectByQuery_complexCondition_returnsUserList() {
        // 1. 构建查询DTO（筛选：status=NORMAL + role=USER；分页：第1页，每页10条）
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setStatus(UserStatusEnum.NORMAL);  // 枚举参数
        queryDTO.setRole(UserRoleEnum.USER);        // 枚举参数
        queryDTO.setPageNum(1);                     // 页码
        queryDTO.setPageSize(10);                  // 每页条数
        queryDTO.setOffset((queryDTO.getPageNum() - 1) * queryDTO.getPageSize());  // 计算偏移量

        // 2. 执行查询方法
        List<User> userList = userMapper.selectByQuery(queryDTO);

        // 3. 断言结果（符合条件的用户：test_buyer(100)/test_seller(90)）
        assertNotNull(userList);
        assertEquals(2, userList.size());
        assertTrue(userList.stream().allMatch(u -> UserStatusEnum.NORMAL.equals(u.getStatus())));
    }

    /**
     * 测试countByQuery：统计复杂条件下的用户总数
     * 适配《代码文档1》2.1.2节 条件查询与统计 - countByQuery方法
     */
    @Test
    void countByQuery_complexCondition_returnsCorrectCount() {
        // 1. 构建查询DTO（筛选：status=BANNED）
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setStatus(UserStatusEnum.BANNED);

        // 2. 执行统计方法
        int count = userMapper.countByQuery(queryDTO);

        // 3. 断言结果（符合条件的用户：test_banned（信用分50））
        assertEquals(1, count, "统计封禁且信用分<80的用户数量错误");
    }

    /**
     * 测试getCount：查询系统总用户数
     * 适配《代码文档1》2.1.2节 条件查询与统计 - getCount方法
     */
    @Test
    void getCount_queryTotalUser_returnsCorrectCount() {
        // 执行统计方法（data-common.sql中初始化了4个用户）
        int total = userMapper.getCount();

        // 断言结果
        assertEquals(4, total, "系统总用户数统计错误");
    }
}