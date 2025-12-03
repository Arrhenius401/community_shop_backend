package com.community_shop.backend.service;

import com.community_shop.backend.convert.OrderConvert;
import com.community_shop.backend.convert.ProductConvert;
import com.community_shop.backend.convert.UserConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.order.*;
import com.community_shop.backend.dto.product.ProductStockUpdateDTO;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.dao.mapper.OrderMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.impl.OrderServiceImpl;
import com.community_shop.backend.utils.SignUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
public class OrderServiceTest {

    // 模拟依赖组件
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserService userService;
    @Mock
    private ProductService productService;
    @Mock
    private MessageService messageService;
    @Mock
    private OrderConvert orderConvert;
    @Mock
    private ProductConvert productConvert;
    @Mock
    private UserConvert userConvert;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private SignUtil signUtil;

    // 注入测试目标服务
    @InjectMocks
    private OrderServiceImpl orderService;

    // 测试数据
    private User testAdminUser;
    private User testBuyerUser;
    private User testSellerUser;
    private User testLowCreditUser;
    private Order testOrder;
    private Product testProduct;
    private OrderCreateDTO testCreateDTO;
    private OrderShipDTO testShipDTO;
    private PayCallbackDTO testPayCallbackDTO;
    private OrderQueryDTO testQueryDTO;

    // 支付回调密钥
    @Value("${pay.callback.secret}")
    private static String ORDER_PAY_CALLBACK_SECRET;

    @BeforeEach
    void setUp() {
        // 初始化测试用户数据
        initTestUsers();
        // 初始化测试商品数据
        initTestProduct();
        // 初始化测试订单数据
        initTestOrder();
        // 初始化测试DTO数据
        initTestDTOs();
        // 注入MyBatis-Plus父类baseMapper
        injectBaseMapper();
        // 模拟Redis依赖行为
        mockRedisBehavior();
        // 模拟convert依赖行为
        mockConvertBehavior();
    }

    /**
     * 初始化测试用户数据
     */
    private void initTestUsers() {
        // 管理员用户
        testAdminUser = new User();
        testAdminUser.setUserId(1L);
        testAdminUser.setUsername("admin");
        testAdminUser.setCreditScore(100);
        testAdminUser.setRole(UserRoleEnum.ADMIN);
        testAdminUser.setCreateTime(LocalDateTime.now().minusMonths(1));

        // 正常买家用户（信用分达标）
        testBuyerUser = new User();
        testBuyerUser.setUserId(2L);
        testBuyerUser.setUsername("testBuyer");
        testBuyerUser.setCreditScore(80);
        testBuyerUser.setRole(UserRoleEnum.USER);
        testBuyerUser.setCreateTime(LocalDateTime.now().minusMonths(2));

        // 卖家用户
        testSellerUser = new User();
        testSellerUser.setUserId(3L);
        testSellerUser.setUsername("testSeller");
        testSellerUser.setCreditScore(90);
        testSellerUser.setRole(UserRoleEnum.USER);
        testSellerUser.setCreateTime(LocalDateTime.now().minusMonths(3));

        // 低信用分用户（信用分不足）
        testLowCreditUser = new User();
        testLowCreditUser.setUserId(4L);
        testLowCreditUser.setUsername("lowCreditUser");
        testLowCreditUser.setCreditScore(50);
        testLowCreditUser.setRole(UserRoleEnum.USER);
        testLowCreditUser.setCreateTime(LocalDateTime.now().minusMonths(1));
    }

    /**
     * 初始化测试商品数据
     */
    private void initTestProduct() {
        testProduct = new Product();
        testProduct.setProductId(1001L);
        testProduct.setSellerId(3L);
        testProduct.setTitle("测试商品");
        testProduct.setCategory("二手手机");
        testProduct.setPrice(BigDecimal.valueOf(1999.0));
        testProduct.setStock(20);
        testProduct.setStatus(ProductStatusEnum.ON_SALE);
        testProduct.setDescription("测试商品描述");
        testProduct.setCreateTime(LocalDateTime.now().minusDays(5));
    }

    /**
     * 初始化测试订单数据
     */
    private void initTestOrder() {
        testOrder = new Order();
        testOrder.setOrderId(2001L);
        testOrder.setOrderNo("2025102412345678");
        testOrder.setBuyerId(2L);
        testOrder.setSellerId(3L);
        testOrder.setProductId(1001L);
        testOrder.setQuantity(2);
        testOrder.setTotalAmount(BigDecimal.valueOf(3998.0));
        testOrder.setAddress("测试收货地址，详细街道信息");
        testOrder.setStatus(OrderStatusEnum.PENDING_PAYMENT);
        testOrder.setPayType(PayTypeEnum.WECHAT_PAY);
        testOrder.setCreateTime(LocalDateTime.now().minusHours(1));
        testOrder.setPayExpireTime(LocalDateTime.now().plusMinutes(29));
    }

    /**
     * 初始化测试DTO数据
     */
    private void initTestDTOs() {
        // 订单创建DTO
        testCreateDTO = new OrderCreateDTO();
        testCreateDTO.setProductId(1001L);
        testCreateDTO.setQuantity(2);
        testCreateDTO.setTotalAmount(BigDecimal.valueOf(3998.0));
        testCreateDTO.setAddress("测试收货地址，详细街道信息");
        testCreateDTO.setBuyerRemark("请尽快发货");
        testCreateDTO.setPayType(PayTypeEnum.WECHAT_PAY);

        // 订单发货DTO
        testShipDTO = new OrderShipDTO();
        testShipDTO.setExpressCompany("顺丰速运");
        testShipDTO.setExpressNo("SF1234567890123");
        testShipDTO.setShipRemark("已仔细包装，请注意查收");

        // 支付回调DTO
        testPayCallbackDTO = new PayCallbackDTO();
        testPayCallbackDTO.setOrderNo("2025102412345678");
        testPayCallbackDTO.setPayAmount(BigDecimal.valueOf(3998.0));
        testPayCallbackDTO.setPayType(PayTypeEnum.WECHAT_PAY);
        testPayCallbackDTO.setPayNo("WX202510241234567890");
        testPayCallbackDTO.setPayStatus("SUCCESS");
        testPayCallbackDTO.setPayTime("2025-10-24 13:00:00");
        testPayCallbackDTO.setSign("testSign123456");

        // 订单查询DTO
        testQueryDTO = new OrderQueryDTO();
        testQueryDTO.setStatus(OrderStatusEnum.PENDING_PAYMENT);
        testQueryDTO.setPageNum(1);
        testQueryDTO.setPageSize(10);
    }

    /**
     * 注入MyBatis-Plus父类的baseMapper字段
     */
    private void injectBaseMapper() {
        try {
            Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class
                    .getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(orderService, orderMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化OrderService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis相关行为
     */
    private void mockRedisBehavior() {
        // 模拟RedisTemplate的opsForValue()返回ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 模拟Redis的set操作
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        // 模拟Redis的delete操作
        doReturn(true).when(redisTemplate).delete(anyString());
        doReturn(1L).when(redisTemplate).delete(anyCollection());
        // 模拟Redis的get操作（默认返回null，可在具体测试方法中覆盖）
        when(valueOperations.get(anyString())).thenReturn(null);
    }

    /**
     * 模拟转换器行为
     */
    private void mockConvertBehavior() {
        // 订单转换与数据库操作
        when(orderConvert.orderCreateDtoToOrder(testCreateDTO)).thenAnswer(invocation -> {
            Order order = new Order();
            BeanUtils.copyProperties(testCreateDTO, order);
            order.setBuyerId(2L);
            order.setSellerId(3L);
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT);
            order.setCreateTime(LocalDateTime.now());
            order.setOrderNo("2025102412345678");
            return order;
        });

        // 商品转换与数据库操作
        when(productConvert.productToProductSimpleDTO(testProduct)).thenAnswer(invocation -> {
            OrderDetailDTO.ProductSimpleDTO simpleDTO = new OrderDetailDTO.ProductSimpleDTO();
            simpleDTO.setProductId(testProduct.getProductId());
            simpleDTO.setTitle(testProduct.getTitle());
            simpleDTO.setQuantity(testCreateDTO.getQuantity());
            simpleDTO.setPrice(testProduct.getPrice());
            return simpleDTO;
        });

        // 用户转换与数据库操作
        when(userConvert.userToBuyerSimpleDTO(testBuyerUser)).thenAnswer(invocation -> {
            OrderDetailDTO.BuyerSimpleDTO buyerSimpleDTO = new OrderDetailDTO.BuyerSimpleDTO();
            buyerSimpleDTO.setUserId(testBuyerUser.getUserId());
            buyerSimpleDTO.setUsername(testBuyerUser.getUsername());
            buyerSimpleDTO.setPhone(testBuyerUser.getPhoneNumber());
            return buyerSimpleDTO;
        });
        when(userConvert.userToSellerSimpleDTO(testSellerUser)).thenAnswer(invocation -> {
            OrderDetailDTO.SellerSimpleDTO sellerSimpleDTO = new OrderDetailDTO.SellerSimpleDTO();
            sellerSimpleDTO.setUserId(testSellerUser.getUserId());
            sellerSimpleDTO.setUsername(testSellerUser.getUsername());
            return sellerSimpleDTO;
        });
    }

    // ==================== 测试用例 ====================

    /**
     * 测试创建订单功能 - 成功场景（买家信用达标、商品库存充足）
     */
    @Test
    void testCreateOrder_Success() {
        // 1. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testBuyerUser);
        when(userService.getById(3L)).thenReturn(testSellerUser);
        when(productService.getById(1001L)).thenReturn(testProduct);

        // 金额一致性校验（实际金额=商品单价×数量）
        BigDecimal actualAmount = testProduct.getPrice().multiply(BigDecimal.valueOf(testCreateDTO.getQuantity()));
        when(productService.updateStock(eq(3L), any(ProductStockUpdateDTO.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        // 订单详情DTO转换与缓存
        OrderDetailDTO detailDTO = new OrderDetailDTO();
        detailDTO.initDefaultValue();
        BeanUtils.copyProperties(testOrder, detailDTO);
        detailDTO.getProduct().setTitle(testProduct.getTitle());
        when(orderConvert.orderToOrderDetailDTO(any(Order.class))).thenReturn(detailDTO);

        // 2. 执行测试方法
        OrderDetailDTO result = orderService.createOrder(2L, testCreateDTO);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testCreateDTO.getTotalAmount(), result.getTotalAmount());
        assertEquals(OrderStatusEnum.PENDING_PAYMENT, result.getStatus());
        assertEquals(testProduct.getTitle(), result.getProduct().getTitle());

        // 4. 验证依赖调用
        verify(userService, times(2)).getById(2L);
        verify(productService, times(2)).getById(1001L);
        verify(productService, times(1)).updateStock(eq(3L), any(ProductStockUpdateDTO.class));
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
//        verify(messageService, times(1)).sendMessage(eq(0L), any(MessageSendDTO.class));
    }

    /**
     * 测试创建订单功能 - 失败场景（买家信用分不足）
     */
    @Test
    void testCreateOrder_CreditTooLow() {
        // 1. 模拟依赖行为（低信用分用户）
        when(userService.getById(4L)).thenReturn(testLowCreditUser);
        when(productService.getById(1001L)).thenReturn(testProduct);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(4L, testCreateDTO);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.CREDIT_TOO_LOW.getCode(), exception.getCode());
        verify(orderMapper, never()).insert(any(Order.class));
        verify(productService, never()).updateStock(anyLong(), any(ProductStockUpdateDTO.class));
    }

    /**
     * 测试创建订单功能 - 失败场景（商品库存不足）
     */
    @Test
    void testCreateOrder_StockInsufficient() {
        // 1. 构造库存不足的商品
        Product stockInsufficientProduct = new Product();
        BeanUtils.copyProperties(testProduct, stockInsufficientProduct);
        stockInsufficientProduct.setStock(1); // 库存1，购买数量2

        // 2. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testBuyerUser);
        when(productService.getById(1001L)).thenReturn(stockInsufficientProduct);

        // 3. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(2L, testCreateDTO);
        });

        // 4. 验证结果
        assertEquals(ErrorCode.PRODUCT_STOCK_INSUFFICIENT.getCode(), exception.getCode());
        verify(orderMapper, never()).insert(any(Order.class));
    }

    /**
     * 测试取消订单功能 - 成功场景（买家取消待支付订单）
     */
    @Test
    void testCancelOrder_Success_Buyer() {
        // 1. 模拟依赖行为（缓存未命中，订单待支付）
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(testOrder);
        when(userService.getById(2L)).thenReturn(testBuyerUser);

        // 恢复库存
        when(productService.getById(1001L)).thenReturn(testProduct);
        when(productService.updateStock(eq(3L), any(ProductStockUpdateDTO.class))).thenReturn(1);

        // 更新订单状态
        Order cancelledOrder = new Order();
        BeanUtils.copyProperties(testOrder, cancelledOrder);
        cancelledOrder.setStatus(OrderStatusEnum.CANCELLED);
        cancelledOrder.setCancelTime(LocalDateTime.now());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // 2. 执行测试方法
        Boolean result = orderService.cancelOrder(2L, 2001L);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(orderMapper, times(1)).selectById(2001L);
        verify(userService, times(1)).getById(2L);
        verify(productService, times(1)).getById(1001L);
        verify(productService, times(1)).updateStock(eq(3L), any(ProductStockUpdateDTO.class));
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(redisTemplate, times(3)).delete(anyString()); // 清除订单详情+买家+卖家列表缓存
//        verify(messageService, times(1)).sendMessage(eq(0L), any(MessageSendDTO.class));
    }

    /**
     * 测试取消订单功能 - 失败场景（非订单买家且非管理员）
     */
    @Test
    void testCancelOrder_NoPermission() {
        // 1. 模拟依赖行为（普通用户取消他人订单）
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(testOrder);
        when(userService.getById(4L)).thenReturn(testLowCreditUser);

        // 2. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(4L, 2001L);
        });

        // 3. 验证结果
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), exception.getCode());
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(productService, never()).updateStock(anyLong(), any(ProductStockUpdateDTO.class));
    }

    /**
     * 测试取消订单功能 - 失败场景（订单状态非待支付）
     */
    @Test
    void testCancelOrder_InvalidStatus() {
        // 1. 构造已支付的订单
        Order paidOrder = new Order();
        BeanUtils.copyProperties(testOrder, paidOrder);
        paidOrder.setStatus(OrderStatusEnum.PENDING_SHIPMENT);

        // 2. 模拟依赖行为
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(paidOrder);
        when(userService.getById(2L)).thenReturn(testBuyerUser);

        // 3. 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(2L, 2001L);
        });

        // 4. 验证结果
        assertEquals(ErrorCode.ORDER_STATUS_INVALID.getCode(), exception.getCode());
        verify(orderMapper, never()).updateById(any(Order.class));
    }

    /**
     * 测试支付回调处理功能 - 成功场景（签名合法、订单状态正常）
     */
    @Test
    void testHandlePayCallback_Success() {
        // 1. 模拟静态方法（需要开启静态模拟）
        try (MockedStatic<SignUtil> signUtilMock = Mockito.mockStatic(SignUtil.class)) {
            // 模拟签名验证通过
            signUtilMock.when(() -> SignUtil.verifySign(any(PayCallbackDTO.class), eq(ORDER_PAY_CALLBACK_SECRET)))
                    .thenReturn(true);

            // 订单查询与状态校验
            Order pendingOrder = new Order();
            BeanUtils.copyProperties(testOrder, pendingOrder);
            pendingOrder.setStatus(OrderStatusEnum.PENDING_PAYMENT);
            when(orderMapper.selectByOrderNo("2025102412345678")).thenReturn(pendingOrder);

            // 金额校验（订单金额与支付金额一致）
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);

            // 2. 执行测试方法
            String result = orderService.handlePayCallback(testPayCallbackDTO);

            // 3. 验证结果
            assertEquals("success", result);

            // 4. 验证依赖调用
            verify(orderMapper, times(1)).selectByOrderNo("2025102412345678");
            verify(orderMapper, times(1)).updateById(any(Order.class));
            verify(redisTemplate, times(3)).delete(anyString());
//            verify(messageService, times(2)).sendMessage(eq(0L), any(MessageSendDTO.class));
        }
    }

    /**
     * 测试支付回调处理功能 - 失败场景（签名验证失败）
     */
    @Test
    void testHandlePayCallback_SignInvalid() {
        // 1. 模拟静态方法（需要开启静态模拟）
        try (MockedStatic<SignUtil> signUtilMock = Mockito.mockStatic(SignUtil.class)) {
            // 关键修复：模拟签名验证失败（返回false）
            signUtilMock.when(() -> SignUtil.verifySign(any(PayCallbackDTO.class), eq(ORDER_PAY_CALLBACK_SECRET)))
                    .thenReturn(false);  // 这里从true改为false

            // 2. 执行测试方法
            String result = orderService.handlePayCallback(testPayCallbackDTO);

            // 3. 验证结果
            assertEquals("fail:签名验证失败", result);
            verify(orderMapper, never()).selectByOrderNo(anyString());
            verify(orderMapper, never()).updateById(any(Order.class));
        }
    }

    /**
     * 测试卖家发货功能 - 成功场景（卖家发货已支付订单）
     */
    @Test
    void testShipOrder_Success_Seller() {
        // 1. 构造已支付的订单
        Order paidOrder = new Order();
        BeanUtils.copyProperties(testOrder, paidOrder);
        paidOrder.setStatus(OrderStatusEnum.PENDING_SHIPMENT);

        // 2. 模拟依赖行为
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(paidOrder);
        when(productService.getById(1001L)).thenReturn(testProduct);

        // 订单发货更新与DTO转换
        Order shippedOrder = new Order();
        BeanUtils.copyProperties(paidOrder, shippedOrder);
        shippedOrder.setStatus(OrderStatusEnum.SHIPPED);
        shippedOrder.setShipTime(LocalDateTime.now());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        OrderDetailDTO detailDTO = new OrderDetailDTO();
        detailDTO.initDefaultValue();
        BeanUtils.copyProperties(shippedOrder, detailDTO);
        detailDTO.getProduct().setTitle(testProduct.getTitle());
//        detailDTO.setExpressCompany("顺丰速运");
//        detailDTO.setExpressNo("SF1234567890123");
        when(orderConvert.orderToOrderDetailDTO(any(Order.class))).thenReturn(detailDTO);

        // 3. 执行测试方法
        OrderDetailDTO result = orderService.shipOrder(3L, 2001L, testShipDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(OrderStatusEnum.SHIPPED, result.getStatus());
//        assertEquals(testShipDTO.getExpressCompany(), result.getExpressCompany());
//        assertEquals(testShipDTO.getExpressNo(), result.getExpressNo());

        // 5. 验证依赖调用
        verify(orderMapper, times(1)).selectById(2001L);
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(redisTemplate, times(2)).delete(anyString()); // 清除订单详情+买家+卖家列表缓存
        verify(redisTemplate, times(3)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
//        verify(messageService, times(1)).sendMessage(eq(0L), any(MessageSendDTO.class));
    }

    /**
     * 测试买家确认收货功能 - 成功场景（买家确认已发货订单）
     */
    @Test
    void testConfirmReceive_Success_Buyer() {
        // 1. 构造已发货的订单
        Order shippedOrder = new Order();
        BeanUtils.copyProperties(testOrder, shippedOrder);
        shippedOrder.setStatus(OrderStatusEnum.SHIPPED);

        // 2. 模拟依赖行为
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(shippedOrder);
        when(userService.getById(2L)).thenReturn(testBuyerUser);
        when(productService.getById(1001L)).thenReturn(testProduct);

        // 订单确认收货更新与DTO转换
        Order completedOrder = new Order();
        BeanUtils.copyProperties(shippedOrder, completedOrder);
        completedOrder.setStatus(OrderStatusEnum.COMPLETED);
        completedOrder.setReceiveTime(LocalDateTime.now());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        OrderDetailDTO detailDTO = new OrderDetailDTO();
        detailDTO.initDefaultValue();
        BeanUtils.copyProperties(completedOrder, detailDTO);
        detailDTO.getProduct().setTitle(testProduct.getTitle());
        when(orderConvert.orderToOrderDetailDTO(any(Order.class))).thenReturn(detailDTO);

        // 3. 执行测试方法
        OrderDetailDTO result = orderService.confirmReceive(2L, 2001L);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(OrderStatusEnum.COMPLETED, result.getStatus());
        assertNotNull(result.getReceiveTime());

        // 5. 验证依赖调用
        verify(orderMapper, times(1)).selectById(2001L);
        verify(userService, times(1)).getById(2L);
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(redisTemplate, times(2)).delete(anyString()); // 清除订单详情+买家+卖家列表缓存
        verify(redisTemplate, times(3)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
//        verify(messageService, times(1)).sendMessage(eq(0L), any(MessageSendDTO.class));
    }

    /**
     * 测试查询订单详情功能 - 成功场景（买家查询自有订单）
     */
    @Test
    void testGetOrderDetail_Success_Buyer() {
        // 1. 模拟依赖行为（缓存未命中）
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(2001L)).thenReturn(testOrder);
        when(userService.getById(2L)).thenReturn(testBuyerUser);
        when(productService.getById(1001L)).thenReturn(testProduct);

        // 订单详情DTO转换
        OrderDetailDTO detailDTO = new OrderDetailDTO();
        BeanUtils.copyProperties(testOrder, detailDTO);
        detailDTO.initDefaultValue();
        detailDTO.getProduct().setTitle(testProduct.getTitle());
        when(orderConvert.orderToOrderDetailDTO(any(Order.class))).thenReturn(detailDTO);

        // 2. 执行测试方法
        OrderDetailDTO result = orderService.getOrderDetail(2L, 2001L);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        assertEquals(testOrder.getBuyerId(), result.getBuyer().getUserId());
        assertEquals(testProduct.getTitle(), result.getProduct().getTitle());

        // 4. 验证依赖调用
        verify(redisTemplate, times(3)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(orderMapper, times(1)).selectById(2001L);
        verify(userService, times(2)).getById(2L);
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    /**
     * 测试买家查询订单列表功能 - 成功场景（从数据库查询）
     */
    @Test
    void testGetBuyerOrders_Success_FromDb() {
        // 1. 准备测试数据
        List<Order> orderList = Arrays.asList(testOrder);
        OrderListItemDTO listItemDTO = new OrderListItemDTO();
        BeanUtils.copyProperties(testOrder, listItemDTO);
        listItemDTO.setProductSummary(testProduct.getTitle());
        listItemDTO.setProductImage("https://test-product.jpg");
        long total = 1;
        long totalPages = 1;

        // 2. 模拟依赖行为（缓存未命中）
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectByBuyerId(eq(2L), eq(OrderStatusEnum.PENDING_PAYMENT), anyInt(), anyInt())).thenReturn(orderList);
        when(orderMapper.countByBuyerId(eq(2L), eq(OrderStatusEnum.PENDING_PAYMENT))).thenReturn((int) total);
        when(productService.getById(1001L)).thenReturn(testProduct);
        when(orderConvert.orderToOrderListItemDTO(any(Order.class))).thenReturn(listItemDTO);

        // 3. 执行测试方法
        PageResult<OrderListItemDTO> result = orderService.getBuyerOrders(2L, testQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(total, result.getTotal());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(1, result.getList().size());
        assertEquals(testOrder.getOrderNo(), result.getList().get(0).getOrderNo());
        assertEquals(testProduct.getTitle(), result.getList().get(0).getProductSummary());

        // 5. 验证依赖调用
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(orderMapper, times(1)).selectByBuyerId(eq(2L), eq(OrderStatusEnum.PENDING_PAYMENT), anyInt(), anyInt());
        verify(orderMapper, times(1)).countByBuyerId(eq(2L), eq(OrderStatusEnum.PENDING_PAYMENT));
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    /**
     * 测试自动关闭超时订单功能 - 成功场景（关闭超时未支付订单）
     */
    @Test
    void testAutoCloseTimeoutOrders_Success() {
        // 1. 准备测试数据（超时未支付订单）
        Order timeoutOrder = new Order();
        BeanUtils.copyProperties(testOrder, timeoutOrder);
        timeoutOrder.setOrderId(2002L);
        timeoutOrder.setCreateTime(LocalDateTime.now().minusMinutes(35));
        timeoutOrder.setPayExpireTime(LocalDateTime.now().minusMinutes(5));
        List<Order> timeoutOrders = Arrays.asList(timeoutOrder);

        // 2. 模拟依赖行为
        when(orderMapper.selectTimeoutPendingOrders(any(LocalDateTime.class))).thenReturn(timeoutOrders);
        when(productService.getById(1001L)).thenReturn(testProduct);
        when(productService.updateStock(eq(3L), any(ProductStockUpdateDTO.class))).thenReturn(1);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // 3. 执行测试方法
        int result = orderService.autoCloseTimeoutOrders(30);

        // 4. 验证结果
        assertEquals(1, result);

        // 5. 验证依赖调用
        verify(orderMapper, times(1)).selectTimeoutPendingOrders(any(LocalDateTime.class));
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(productService, times(1)).getById(1001L);
        verify(productService, times(1)).updateStock(eq(1001L), any(ProductStockUpdateDTO.class));
        verify(redisTemplate, times(3)).delete(anyString()); // 清除订单详情+买家+卖家列表缓存
//        verify(messageService, times(1)).sendMessage(eq(0L), any(MessageSendDTO.class));
    }
}