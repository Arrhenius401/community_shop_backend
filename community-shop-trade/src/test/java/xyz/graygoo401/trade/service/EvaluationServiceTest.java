package xyz.graygoo401.trade.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import xyz.graygoo401.api.trade.dto.evaluation.*;
import xyz.graygoo401.api.trade.enums.EvaluationSortFieldEnum;
import xyz.graygoo401.api.trade.enums.EvaluationStatusEnum;
import xyz.graygoo401.api.trade.enums.OrderStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.api.user.util.UserUtil;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.enums.SortDirectionEnum;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.exception.error.SystemErrorCode;
import xyz.graygoo401.trade.convert.EvaluationConvert;
import xyz.graygoo401.trade.dao.entity.Evaluation;
import xyz.graygoo401.trade.dao.entity.Order;
import xyz.graygoo401.trade.dao.entity.Product;
import xyz.graygoo401.trade.dao.mapper.EvaluationMapper;
import xyz.graygoo401.trade.exception.error.EvaluationErrorCode;
import xyz.graygoo401.trade.exception.error.OrderErrorCode;
import xyz.graygoo401.trade.service.base.OrderService;
import xyz.graygoo401.trade.service.base.ProductService;
import xyz.graygoo401.trade.service.impl.EvaluationServiceImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EvaluationServiceTest {

    // 模拟依赖组件
    @Mock
    private EvaluationMapper evaluationMapper;
    @Mock
    private UserUtil userService;
    @Mock
    private OrderService orderService; // 依赖订单服务校验订单状态
    @Mock
    private ProductService productService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private EvaluationConvert evaluationConvert;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    // 注入测试目标服务
    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    // 测试数据
    private UserDTO testBuyer;        // 买家用户（评价发起者）
    private UserDTO testAdmin;        // 管理员用户（用于删除评价权限场景）
    private UserDTO testSeller;       // 卖家用户（被评价方，无评价操作权限）
    private Evaluation testEvaluation; // 测试评价实体
    private EvaluationCreateDTO testCreateDTO;     // 评价提交DTO
    private EvaluationUpdateDTO testUpdateDTO;     // 评价更新DTO
    private EvaluationQueryDTO testQueryDTO;       // 评价查询DTO
    private Order testOrder;                       // 关联订单（已完成状态，允许评价）
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 1. 初始化测试数据
        initTestUsers();
        initTestOrder();
        initTestEvaluation();
        initTestProduct();
        initTestDTOs();

        // 2. 注入MyBatis-Plus父类baseMapper（解决ServiceImpl继承问题）
        injectBaseMapper();

        // 3. 模拟Redis依赖行为（严格区分void/非void方法）
        mockRedisBehavior();
    }

    /**
     * 初始化测试用户数据
     */
    private void initTestUsers() {
        // 买家用户（ID=1，已完成订单，有权提交评价）
        testBuyer = new UserDTO();
        testBuyer.setUserId(1L);
        testBuyer.setUsername("testBuyer");
        testBuyer.setRole(UserRoleEnum.USER);
        testBuyer.setAvatarUrl("https://buyer-avatar.jpg");

        // 管理员用户（ID=999，有权删除任意评价）
        testAdmin = new UserDTO();
        testAdmin.setUserId(999L);
        testAdmin.setUsername("admin");
        testAdmin.setRole(UserRoleEnum.ADMIN);

        // 卖家用户（ID=2，被评价方，无评价操作权限）
        testSeller = new UserDTO();
        testSeller.setUserId(2L);
        testSeller.setUsername("testSeller");
        testSeller.setRole(UserRoleEnum.USER);
    }

    /**
     * 初始化测试订单数据（已完成状态，允许提交评价）
     */
    private void initTestOrder() {
        testOrder = new Order();
        testOrder.setOrderId(1001L);          // 订单ID
        testOrder.setBuyerId(1L);             // 买家ID（与testBuyer一致）
        testOrder.setSellerId(2L);           // 卖家ID
        testOrder.setProductId(2001L);       // 关联商品ID
        testOrder.setStatus(OrderStatusEnum.COMPLETED);     // 订单状态：已完成（允许评价）
        testOrder.setCreateTime(LocalDateTime.now().minusDays(2));
        testOrder.setReceiveTime(LocalDateTime.now().minusDays(1));
    }

    /**
     * 初始化测试评价实体
     */
    private void initTestEvaluation() {
        testEvaluation = new Evaluation();
        testEvaluation.setEvalId(3001L);          // 评价ID
        testEvaluation.setOrderId(1001L);        // 关联订单ID
        testEvaluation.setUserId(1L);           // 评价人ID（买家）
        testEvaluation.setEvaluateeId(2L);         // 被评价人ID（卖家）
        testEvaluation.setScore(5);             // 评分：5星（好评）
        testEvaluation.setContent("商品质量很好，物流很快！");
        testEvaluation.setStatus(EvaluationStatusEnum.NORMAL); // 状态：可见
        testEvaluation.setCreateTime(LocalDateTime.now().minusHours(1));
    }

    /**
     * 测试数据：评价提交DTO（买家1对订单1001提交5星评价）
     */
    private void initTestProduct() {
        testProduct = new Product();
        testProduct.setProductId(2001L);
        testProduct.setSellerId(2L);
        testProduct.setTitle("测试商品");
        testProduct.setCategory("测试类目");
        testProduct.setDescription("这是测试商品");
        testProduct.setPrice(BigDecimal.valueOf(9.99));
        testProduct.setStock(100);
        testProduct.setViewCount(0);
        testProduct.setCreateTime(LocalDateTime.now().minusDays(3));
    }

    /**
     * 初始化测试DTO数据
     */
    private void initTestDTOs() {
        // 1. 评价提交DTO（买家1对订单1001提交5星评价）
        testCreateDTO = new EvaluationCreateDTO();
        testCreateDTO.setOrderId(1001L);
        testCreateDTO.setScore(5);
        testCreateDTO.setContent("商品质量很好，物流很快！");
        testCreateDTO.setTags(Arrays.asList("质量好", "物流快"));
        testCreateDTO.setImageUrls(Arrays.asList("https://eval-img1.jpg", "https://eval-img2.jpg"));

        // 2. 评价更新DTO（更新评价内容和评分）
        testUpdateDTO = new EvaluationUpdateDTO();
        testUpdateDTO.setEvalId(3001L);
        testUpdateDTO.setNewScore(4);
        testUpdateDTO.setNewContent("商品质量不错，物流略有延迟");

        // 3. 评价查询DTO（查询卖家2的评价，分页参数）
        testQueryDTO = new EvaluationQueryDTO();
        testQueryDTO.setEvaluateeId(2L);
        testQueryDTO.setPageNum(1);
        testQueryDTO.setPageSize(10);
        testQueryDTO.setSortField(EvaluationSortFieldEnum.CREATE_TIME);
        testQueryDTO.setSortDir(SortDirectionEnum.DESC);
    }

    /**
     * 注入MyBatis-Plus父类的baseMapper字段（解决Service继承ServiceImpl问题）
     */
    private void injectBaseMapper() {
        try {
            // 获取MyBatis-Plus ServiceImpl父类的baseMapper字段
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            // 为测试服务注入mock的evaluationMapper
            baseMapperField.set(evaluationService, evaluationMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化EvaluationService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis相关行为（严格区分void/非void方法，避免doNothing()错误）
     */
    private void mockRedisBehavior() {
        // 1. 模拟RedisTemplate.opsForValue()返回ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 2. 模拟Redis set操作（void方法，可用doNothing()）
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // 3. 模拟Redis get操作（默认返回null，具体测试用例可覆盖）
        when(valueOperations.get(anyString())).thenReturn(null);

        // 4. 模拟Redis delete操作（非void方法，返回Boolean/Long）
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.delete(anyCollection())).thenReturn(1L);
    }

    // ==================== 测试用例：核心业务场景 ====================

    /**
     * 测试1：提交评价 - 成功场景（订单已完成+未评价+买家本人操作）
     * 校验点：订单状态校验、未重复评价、主键回填、DTO转换正确
     */
    @Test
    void testSubmitEvaluation_Success() {
        // 1. 模拟依赖行为
        // 校验买家存在
        when(userService.getUserById(1L)).thenReturn(testBuyer);
        // 校验订单存在且已完成
        when(orderService.getById(1001L)).thenReturn(testOrder);
        // 校验订单未评价（返回null）
        when(evaluationMapper.selectByOrderId(1001L)).thenReturn(null);

        // 捕获插入的Evaluation对象，手动模拟主键回填
        ArgumentCaptor<Evaluation> evaluationCaptor = ArgumentCaptor.forClass(Evaluation.class);
        when(evaluationMapper.insert(evaluationCaptor.capture())).thenAnswer(invocation -> {
            Evaluation capturedEval = evaluationCaptor.getValue();
            capturedEval.setEvalId(3001L); // 手动设置评价ID（模拟数据库自增）
            return 1; // 返回插入成功行数
        });

        // 模拟查询插入后的评价
        when(evaluationMapper.selectById(3001L)).thenReturn(testEvaluation);

        // 模拟DTO转换（CreateDTO→Entity）
        when(evaluationConvert.evaluationCreateDtoToEvaluation(any(EvaluationCreateDTO.class))).thenAnswer(invocation -> {
            EvaluationCreateDTO dto = invocation.getArgument(0);
            Evaluation eval = new Evaluation();
            eval.setOrderId(dto.getOrderId());
            eval.setScore(dto.getScore());
            eval.setContent(dto.getContent());
            eval.setUserId(1L);
            eval.setEvaluateeId(2L);
            eval.setStatus(EvaluationStatusEnum.NORMAL);
            return eval;
        });

        // 模拟DTO转换（Entity→DetailDTO）
        when(evaluationConvert.evaluationToEvaluationDetailDTO(any(Evaluation.class))).thenAnswer(invocation -> {
            Evaluation eval = invocation.getArgument(0);
            EvaluationDetailDTO dto = new EvaluationDetailDTO();
            dto.setEvalId(eval.getEvalId());
            dto.setOrderId(eval.getOrderId());
            dto.setScore(eval.getScore());
            dto.setContent(eval.getContent());
            dto.setStatus(eval.getStatus());
            // 封装评价人脱敏信息
            EvaluationDetailDTO.EvaluatorDTO evaluatorDTO = new EvaluationDetailDTO.EvaluatorDTO();
            evaluatorDTO.setUserId(eval.getUserId());
            evaluatorDTO.setUsername("testBuyer");
            evaluatorDTO.setAvatarUrl("https://buyer-avatar.jpg");
            dto.setEvaluator(evaluatorDTO);
            // 封装商品简易信息
//            EvaluationDetailDTO.ProductSimpleDTO productDTO = new EvaluationDetailDTO.ProductSimpleDTO();
//            productDTO.setProductId(eval.getProductId());
//            productDTO.setProductName("测试商品");
//            productDTO.setProductImage("https://product-img.jpg");
//            dto.setProduct(productDTO);
            return dto;
        });

        // 模拟商品信息
        when(productService.getById(2001L)).thenReturn(testProduct);

        // 模拟卖家信息
        when(userService.getUserById(2L)).thenReturn(testSeller);

        // 2. 执行测试方法（买家ID=1，评价参数=testCreateDTO）
        EvaluationDetailDTO result = evaluationService.submitEvaluation(1L, testCreateDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(3001L, result.getEvalId());
        assertEquals(testCreateDTO.getScore(), result.getScore());
        assertEquals(testCreateDTO.getContent(), result.getContent());
        assertEquals(EvaluationStatusEnum.NORMAL, result.getStatus());
        assertEquals("testBuyer", result.getEvaluator().getUsername());

        // 4. 验证依赖调用
        verify(userService, times(1)).getUserById(1L); // 校验买家存在
        verify(orderService, times(1)).getById(1001L); // 校验订单存在
        verify(evaluationMapper, times(1)).selectByOrderId(1001L); // 校验未重复评价
        verify(evaluationMapper, times(1)).insert(any(Evaluation.class)); // 插入评价
        verify(evaluationMapper, times(0)).selectById(3001L); // 查询插入后的评价
        verify(redisTemplate, times(1)).delete(anyString()); // 清除卖家评分缓存+商品评分缓存
    }

    /**
     * 测试2：提交评价 - 失败场景（订单未完成）
     * 校验点：抛出ORDER_STATUS_NOT_COMPLETED异常，未执行评价插入
     */
    @Test
    void testSubmitEvaluation_Fail_OrderNotCompleted() {
        // 1. 准备未完成订单
        Order uncompletedOrder = new Order();
        uncompletedOrder.setOrderId(1002L);
        uncompletedOrder.setBuyerId(1L);
        uncompletedOrder.setStatus(OrderStatusEnum.PENDING_PAYMENT); // 订单状态：待支付（未完成）

        // 2. 模拟依赖行为
        when(userService.getUserById(1L)).thenReturn(testBuyer);
        when(orderService.getById(1002L)).thenReturn(uncompletedOrder);

        // 3. 构造未完成订单的评价DTO
        EvaluationCreateDTO uncompletedEvalDTO = new EvaluationCreateDTO();
        uncompletedEvalDTO.setOrderId(1002L);
        uncompletedEvalDTO.setScore(4);
        uncompletedEvalDTO.setContent("测试未完成订单评价");

        // 4. 执行测试并捕获异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            evaluationService.submitEvaluation(1L, uncompletedEvalDTO);
        });

        // 5. 验证异常信息
        assertEquals(OrderErrorCode.ORDER_NOT_COMPLETED.getCode(), exception.getErrorCode());

        // 6. 验证依赖调用（未执行评价插入）
        verify(evaluationMapper, never()).insert(any(Evaluation.class));
    }

    /**
     * 测试3：提交评价 - 失败场景（订单已评价）
     * 校验点：抛出ORDER_ALREADY_EVALUATED异常，未执行评价插入
     */
    @Test
    void testSubmitEvaluation_Fail_OrderAlreadyEvaluated() {
        // 1. 模拟依赖行为
        when(userService.getUserById(1L)).thenReturn(testBuyer);
        when(orderService.getById(1001L)).thenReturn(testOrder);
        when(evaluationMapper.selectByOrderId(1001L)).thenReturn(testEvaluation); // 订单已评价

        // 2. 执行测试并捕获异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            evaluationService.submitEvaluation(1L, testCreateDTO);
        });

        // 3. 验证异常信息
        assertEquals(EvaluationErrorCode.ORDER_ALREADY_EVALUATED, exception.getErrorCode());

        // 4. 验证依赖调用（未执行评价插入）
        verify(evaluationMapper, never()).insert(any(Evaluation.class));
    }

    /**
     * 测试4：更新评价内容 - 成功场景（评价人本人操作）
     * 校验点：评价存在、权限校验、更新后内容正确
     */
    @Test
    void testUpdateEvaluationContent_Success_Buyer() {
        // 1. 准备更新后的评价对象
        Evaluation updatedEvaluation = new Evaluation();
        BeanUtils.copyProperties(testEvaluation, updatedEvaluation);
        updatedEvaluation.setScore(testUpdateDTO.getNewScore());
        updatedEvaluation.setContent(testUpdateDTO.getNewContent());
        updatedEvaluation.setUpdateTime(LocalDateTime.now());

        // 2. 模拟依赖行为
        when(evaluationMapper.selectById(3001L)).thenReturn(testEvaluation); // 评价存在
        when(evaluationMapper.updateById(any(Evaluation.class))).thenReturn(1); // 更新成功
        when(evaluationMapper.selectById(3001L)).thenReturn(updatedEvaluation); // 查询更新后评价

        // 3. 执行测试方法（买家ID=1，更新参数=testUpdateDTO）
        Boolean result = evaluationService.updateEvaluationContent(1L, testUpdateDTO);

        // 4. 验证结果
        assertTrue(result);

        // 5. 验证依赖调用
        verify(evaluationMapper, times(1)).selectById(3001L); // 两次查询（原评价+更新后评价）
        verify(evaluationMapper, times(1)).updateById(any(Evaluation.class)); // 执行更新
        verify(redisTemplate, times(0)).delete(anyString()); // 清除卖家评分缓存+商品评分缓存
    }

    /**
     * 测试5：更新评价内容 - 失败场景（非评价人操作）
     * 校验点：抛出PERMISSION_DENIED异常，未执行更新
     */
    @Test
    void testUpdateEvaluationContent_Fail_NoPermission() {
        // 1. 模拟依赖行为（评价存在，但操作人是卖家）
        when(evaluationMapper.selectById(3001L)).thenReturn(testEvaluation);
        when(userService.getUserById(2L)).thenReturn(testSeller); // 卖家操作

        // 2. 执行测试并捕获异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            evaluationService.updateEvaluationContent(2L, testUpdateDTO); // 卖家ID=2，非评价人
        });

        // 3. 验证异常信息
        assertEquals(SystemErrorCode.PERMISSION_DENIED, exception.getErrorCode());

        // 4. 验证依赖调用（未执行更新）
        verify(evaluationMapper, never()).updateById(any(Evaluation.class));
    }

    /**
     * 测试6：删除评价 - 成功场景（管理员操作）
     * 校验点：管理员权限校验、逻辑删除、缓存清除
     */
    @Test
    void testDeleteEvaluationById_Success_Admin() {
        // 1. 模拟依赖行为
        when(evaluationMapper.selectById(3001L)).thenReturn(testEvaluation); // 评价存在
        when(userService.getUserById(999L)).thenReturn(testAdmin); // 管理员用户
        when(evaluationMapper.deleteById(3001L)).thenReturn(1); // 删除成功

        // 2. 执行测试方法（管理员ID=999，评价ID=3001）
        Boolean result = evaluationService.deleteEvaluationById(3001L, 999L);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(evaluationMapper, times(1)).selectById(3001L); // 查询评价
        verify(userService, times(1)).getUserById(999L); // 校验管理员身份
        verify(evaluationMapper, times(1)).deleteById(3001L); // 执行删除
        verify(redisTemplate, times(0)).delete(anyString()); // 清除卖家评分缓存+商品评分缓存
    }

    /**
     * 测试7：查询卖家评价列表 - 成功场景（分页+筛选）
     * 校验点：分页参数正确、总条数匹配、DTO转换正确
     */
    @Test
    void testSearchEvaluationsByQuery_Success() {
        // 1. 准备测试数据
        List<Evaluation> evaluationList = Arrays.asList(testEvaluation);
        EvaluationListItemDTO listItemDTO = new EvaluationListItemDTO();
        listItemDTO.setEvalId(3001L);
        listItemDTO.setScore(5);
        listItemDTO.setContentSummary("商品质量很好，物流很快！");
        listItemDTO.setCreateTime(testEvaluation.getCreateTime());
        // 封装评价人脱敏信息
        EvaluationListItemDTO.EvaluatorSimpleDTO evaluatorDTO = new EvaluationListItemDTO.EvaluatorSimpleDTO();
        evaluatorDTO.setUsername("testBuyer");
        evaluatorDTO.setAvatarUrl("https://buyer-avatar.jpg");
        listItemDTO.setEvaluator(evaluatorDTO);

        // 2. 模拟依赖行为
        when(evaluationMapper.countByQuery(testQueryDTO)).thenReturn(1); // 总条数=1
        when(evaluationMapper.selectByQuery(testQueryDTO)).thenReturn(evaluationList); // 评价列表
        when(evaluationConvert.evaluationToEvaluationListItemDTO(any(Evaluation.class))).thenReturn(listItemDTO);
        when(userService.getUserById(2L)).thenReturn(testSeller);    // 返回卖家用户

        // 3. 执行测试方法（查询参数=testQueryDTO）
        PageResult<EvaluationListItemDTO> result = evaluationService.searchEvaluationsByQuery(testQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal()); // 总条数=1
        assertEquals(1, result.getList().size()); // 列表长度=1
        assertEquals(3001L, result.getList().get(0).getEvalId());
        assertEquals(5, result.getList().get(0).getScore());

        // 5. 验证依赖调用
        verify(evaluationMapper, times(1)).countByQuery(testQueryDTO); // 统计总数
        verify(evaluationMapper, times(1)).selectByQuery(testQueryDTO); // 查询列表
//        verify(evaluationConvert, times(1)).evaluationToEvaluationListItemDTO(any(Evaluation.class)); // DTO转换
    }

    /**
     * 测试8：计算卖家评分 - 成功场景
     * 校验点：平均评分正确、好评率计算正确、各星级数量匹配
     */
    @Test
    void testCalculateSellerScore_Success() {
        // 1. 模拟依赖行为（卖家2的评分统计）
        when(evaluationMapper.selectSellerAverageScore(2L)).thenReturn(4.5); // 平均评分4.5
        when(evaluationMapper.countSellerScoreLevel(2L, 5, 5)).thenReturn(10); // 五星10条
        when(evaluationMapper.countSellerScoreLevel(2L, 4, 4)).thenReturn(5);  // 四星5条
        when(evaluationMapper.countSellerScoreLevel(2L, 3, 3)).thenReturn(3);  // 三星3条
        when(evaluationMapper.countSellerScoreLevel(2L, 2, 2)).thenReturn(1);  // 二星1条
        when(evaluationMapper.countSellerScoreLevel(2L, 1, 1)).thenReturn(1);  // 一星1条

        // 2. 执行测试方法（卖家ID=2）
        SellerScoreDTO result = evaluationService.calculateSellerScore(2L);

        // 3. 验证结果（总评价数=10+5+3+1+1=20；好评率=(10+5)/20=75%）
        assertNotNull(result);
        assertEquals(2L, result.getSellerId());
        assertEquals(4.5, result.getAverageScore());
        assertEquals(20, result.getTotalCount());
        assertEquals(75.0, result.getPositiveRate()); // 好评率75%
        assertEquals(10, result.getFiveStarCount());
        assertEquals(1, result.getOneStarCount());

        // 4. 验证依赖调用
        verify(evaluationMapper, times(1)).selectSellerAverageScore(2L); // 平均评分
        verify(evaluationMapper, times(5)).countSellerScoreLevel(anyLong(), anyInt(), anyInt()); // 各星级数量
    }

    /**
     * 测试9：计算商品评分 - 成功场景
     * 校验点：平均评分正确、各星级数量匹配
     */
    @Test
    void testCalculateProductScore_Success() {
        // 1. 模拟依赖行为（商品2001的评分统计）
        when(evaluationMapper.selectProductAverageScore(2001L)).thenReturn(4.5); // 平均评分4.5
        when(evaluationMapper.countProductScoreLevel(2001L, 5, 5)).thenReturn(9);  // 五星9条
        when(evaluationMapper.countProductScoreLevel(2001L, 4, 4)).thenReturn(6);  // 四星6条
        when(evaluationMapper.countProductScoreLevel(2001L, 3, 3)).thenReturn(3);  // 三星3条
        when(evaluationMapper.countProductScoreLevel(2001L, 2, 2)).thenReturn(1);  // 二星1条
        when(evaluationMapper.countProductScoreLevel(2001L, 1, 1)).thenReturn(1);  // 一星1条

        // 2. 执行测试方法（商品ID=2001）
        ProductScoreDTO result = evaluationService.calculateProductScore(2001L);

        // 3. 验证结果（总评价数=8+6+2+1+1=18；好评率=(8+6)/18≈77.8%）
        assertNotNull(result);
        assertEquals(2001L, result.getProductId());
        assertEquals(4.5, result.getAverageScore(), 0.1);
        assertEquals(20, result.getTotalCount());
        assertEquals(75, result.getPositiveRate(), 0.1); // 好评率≈75.0%
        assertEquals(9, result.getFiveStarCount());
        assertEquals(1, result.getOneStarCount());

        // 4. 验证依赖调用
        verify(evaluationMapper, times(1)).selectProductAverageScore(2001L); // 平均评分
        verify(evaluationMapper, times(5)).countProductScoreLevel(anyLong(), anyInt(), anyInt()); // 各星级数量

    }
}