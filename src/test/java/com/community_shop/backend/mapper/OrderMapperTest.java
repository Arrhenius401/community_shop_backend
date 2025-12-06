package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community_shop.backend.dao.mapper.OrderMapper;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.enums.code.OrderStatusEnum;
import com.community_shop.backend.enums.simple.PayTypeEnum;
import com.community_shop.backend.dto.order.OrderQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.5节 OrderMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.1节 Order实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 * 4. 《测试文档1 基础SQL脚本设计.docx》ORDER模块初始化数据
 */
@MybatisPlusTest  // 仅加载MyBatis相关Bean，轻量化测试
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（加载application-test.properties）
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;  // 注入待测试的OrderMapper

    // 测试复用的基础数据（从data-order.sql初始化数据中获取）
    private Order pendingPayOrder;    // 待支付订单（orderId=1，status=PENDING_PAYMENT）
    private Order paidUnshippedOrder;// 已支付未发货订单（orderId=2，status=PAID，payType=WECHAT）
    private Order completedOrder;     // 已完成订单（orderId=3，status=COMPLETED，payType=ALIPAY）
    private Order cancelledOrder;     // 已取消订单（orderId=4，status=CANCELLED）

    /**
     * 测试前初始化：从数据库查询基础测试订单，确保与data-order.sql数据一致
     * 适配《代码文档0》中Order实体的枚举属性（status/payType）与业务属性（totalAmount/quantity）
     */
    @BeforeEach
    void setUp() {
        // 按orderId查询（基于BaseMapper的selectById方法）
        pendingPayOrder = orderMapper.selectById(1L);
        paidUnshippedOrder = orderMapper.selectById(2L);
        completedOrder = orderMapper.selectById(3L);
        cancelledOrder = orderMapper.selectById(4L);

        // 断言初始化成功（确保ORDER模块SQL脚本已正确执行）
        assertNotNull(pendingPayOrder, "初始化失败：未查询到待支付订单（data-order.sql中orderId=1）");
        assertNotNull(paidUnshippedOrder, "初始化失败：未查询到已支付未发货订单（data-order.sql中orderId=2）");
        assertNotNull(completedOrder, "初始化失败：未查询到已完成订单（data-order.sql中orderId=3）");
        assertNotNull(cancelledOrder, "初始化失败：未查询到已取消订单（data-order.sql中orderId=4）");
    }

    /**
     * 测试selectById：查询订单详情（正常场景）
     * 适配《代码文档1》2.5.2节 订单创建与基础查询 - selectById方法
     */
    @Test
    void selectById_existOrderId_returnsOrderDetail() {
        // 1. 执行测试方法（查询已完成订单orderId=3）
        Order result = orderMapper.selectById(3L);

        // 2. 断言结果（匹配data-order.sql中已完成订单数据）
        assertNotNull(result);
        assertEquals(completedOrder.getOrderId(), result.getOrderId());
        assertEquals(2L, result.getProductId(), "商品ID应为小米手环8（productId=2）");
        assertEquals(1L, result.getBuyerId(), "买家ID应为test_buyer（userId=1）");
        assertEquals(2L, result.getSellerId(), "卖家ID应为test_seller（userId=2）");
        assertEquals("ORDER20240106001", result.getOrderNo(), "订单编号应匹配初始化数据");
        assertEquals(new BigDecimal("299.00"), result.getTotalAmount(), "订单金额应为299.00");
        assertEquals(1, result.getQuantity(), "购买数量应为1");
        assertEquals(OrderStatusEnum.COMPLETED, result.getStatus(), "订单状态应为COMPLETED");
        assertEquals(PayTypeEnum.ALIPAY, result.getPayType(), "支付方式应为ALIPAY");
        assertNotNull(result.getPayTime(), "已完成订单应有支付时间");
        assertNotNull(result.getShipTime(), "已完成订单应有发货时间");
        assertNotNull(result.getReceiveTime(), "已完成订单应有收货时间");
    }

    /**
     * 测试selectById：查询不存在的订单（异常场景）
     */
    @Test
    void selectById_nonExistOrderId_returnsNull() {
        // 执行测试方法（查询不存在的orderId=100）
        Order result = orderMapper.selectById(100L);
        assertNull(result, "查询不存在的订单应返回null");
    }

    /**
     * 测试selectByOrderNo：按订单号查询订单（正常场景）
     * 适配《代码文档1》2.5.2节 订单创建与基础查询 - selectByOrderNo方法
     */
    @Test
    void selectByOrderNo_existOrderNo_returnsOrder() {
        // 1. 执行测试方法（查询已支付订单的订单号ORDER20240107002）
        Order result = orderMapper.selectByOrderNo("ORDER20240107002");

        // 2. 断言结果
        assertNotNull(result);
        assertEquals(paidUnshippedOrder.getOrderId(), result.getOrderId());
        assertEquals("ORDER20240107002", result.getOrderNo(), "订单号应完全匹配");
        assertEquals(OrderStatusEnum.PENDING_SHIPMENT, result.getStatus(), "订单状态应为PENDING_SHIPMENT");
    }

    /**
     * 测试selectByBuyerId：买家分页查询订单（正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - selectByBuyerId方法
     */
    @Test
    void selectByBuyerId_existBuyerId_returnsBuyerOrderList() {
        // 1. 执行测试方法（查询test_buyer（userId=1）的所有订单，分页：offset=0，limit=10）
        List<Order> buyerOrderList = orderMapper.selectByBuyerId(1L, null, 0, 10);

        // 2. 断言结果（data-order.sql中buyerId=1有4个订单）
        assertNotNull(buyerOrderList);
        assertEquals(5, buyerOrderList.size(), "买家userId=1应拥有4个订单");

        // 验证订单状态覆盖所有类型
        boolean hasPending = buyerOrderList.stream().anyMatch(o -> OrderStatusEnum.PENDING_PAYMENT.equals(o.getStatus()));
        boolean hasPaid = buyerOrderList.stream().anyMatch(o -> OrderStatusEnum.PENDING_SHIPMENT.equals(o.getStatus()));
        boolean hasReceived = buyerOrderList.stream().anyMatch(o -> OrderStatusEnum.COMPLETED.equals(o.getStatus()));
        boolean hasCancelled = buyerOrderList.stream().anyMatch(o -> OrderStatusEnum.CANCELLED.equals(o.getStatus()));
        assertTrue(hasPending, "订单列表应包含待支付订单");
        assertTrue(hasPaid, "订单列表应包含已支付订单");
        assertTrue(hasReceived, "订单列表应包含已完成订单");
        assertTrue(hasCancelled, "订单列表应包含已取消订单");
    }

    /**
     * 测试selectByBuyerId：按状态筛选买家订单（正常场景）
     */
    @Test
    void selectByBuyerId_filterByStatus_returnsMatchedList() {
        // 1. 执行测试方法（查询test_buyer的已取消订单，分页：offset=0，limit=10）
        List<Order> cancelledList = orderMapper.selectByBuyerId(1L, OrderStatusEnum.CANCELLED, 0, 10);

        // 2. 断言结果（data-order.sql中买家仅orderId=4为已取消订单）
        assertNotNull(cancelledList);
        assertEquals(1, cancelledList.size(), "买家已取消订单数量应为1");
        assertEquals(cancelledOrder.getOrderId(), cancelledList.get(0).getOrderId());
    }

    /**
     * 测试selectBySellerId：卖家分页查询订单（正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - selectBySellerId方法
     */
    @Test
    void selectBySellerId_existSellerId_returnsSellerOrderList() {
        // 1. 执行测试方法（查询test_seller（userId=2）的已完成订单，分页：offset=0，limit=10）
        List<Order> sellerOrderList = orderMapper.selectBySellerId(2L, OrderStatusEnum.COMPLETED, 0, 10);

        // 2. 断言结果（data-order.sql中卖家仅orderId=3为已完成订单）
        assertNotNull(sellerOrderList);
        assertEquals(1, sellerOrderList.size(), "卖家已完成订单数量应为1");
        assertEquals(completedOrder.getOrderId(), sellerOrderList.get(0).getOrderId());
        assertEquals(OrderStatusEnum.COMPLETED, sellerOrderList.get(0).getStatus(), "订单状态应为COMPLETED");
    }

    /**
     * 测试updateStatus：更新订单状态为已取消（枚举参数，正常场景）
     * 适配《代码文档1》2.5.2节 状态与时间更新 - updateStatus方法
     * 适配《中间件文档3》枚举TypeHandler自动转换（枚举→数据库code）
     */
    @Test
    void updateStatus_changeToCancelled_returnsAffectedRows1() {
        // 1. 准备参数（将待支付订单orderId=1的状态从PENDING_PAYMENT改为CANCELLED）
        Long orderId = 1L;
        OrderStatusEnum newStatus = OrderStatusEnum.CANCELLED;

        // 2. 执行更新方法（直接传递枚举对象，TypeHandler自动转换为"CANCELLED"）
        int affectedRows = orderMapper.updateStatus(orderId, newStatus);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新订单状态应影响1行数据");

        // 4. 验证状态已更新（查询结果自动转换为枚举）
        Order updatedOrder = orderMapper.selectById(orderId);
        assertEquals(newStatus, updatedOrder.getStatus(), "订单状态未更新为CANCELLED");
    }

    /**
     * 测试updatePayInfo：更新订单支付信息（正常场景）
     * 适配《代码文档1》2.5.2节 状态与时间更新 - updatePayInfo方法
     */
    @Test
    void updatePayInfo_validParam_returnsAffectedRows1() {
        // 1. 准备参数（为待支付订单orderId=1添加支付信息：微信支付，当前时间为支付时间）
        Long orderId = 1L;
        LocalDateTime payTime = LocalDateTime.now();
        PayTypeEnum payType = PayTypeEnum.WECHAT_PAY;

        // 2. 执行更新方法
        int affectedRows = orderMapper.updatePayInfo(orderId, payTime, payType);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新支付信息应影响1行数据");

        // 4. 验证支付信息已更新
        Order updatedOrder = orderMapper.selectById(orderId);
        assertEquals(payType, updatedOrder.getPayType(), "支付方式未更新为WECHAT");
        assertNotNull(updatedOrder.getPayTime(), "支付时间不应为null");
        // 验证订单状态是否同步更新（若业务逻辑包含状态变更，需补充断言）
    }

    /**
     * 测试updateShipTime：更新订单发货时间（正常场景）
     * 适配《代码文档1》2.5.2节 状态与时间更新 - updateShipTime方法
     */
    @Test
    void updateShipInfo_validParam_returnsAffectedRows1() {
        // 1. 准备参数（为已支付订单orderId=2添加发货时间：当前时间）
        Long orderId = 2L;
        LocalDateTime shipTime = LocalDateTime.now();

        // 2. 执行更新方法
        int affectedRows = orderMapper.updateShipInfo(orderId, shipTime);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新发货时间应影响1行数据");

        // 4. 验证发货时间已更新
        Order updatedOrder = orderMapper.selectById(orderId);
        assertNotNull(updatedOrder.getShipTime(), "发货时间不应为null");
        assertEquals(shipTime.toLocalDate(), updatedOrder.getShipTime().toLocalDate(), "发货日期应匹配");
    }

    /**
     * 测试updateReceiveTime：更新订单收货时间（正常场景）
     * 适配《代码文档1》2.5.2节 状态与时间更新 - updateReceiveTime方法
     */
    @Test
    void updateReceiveInfo_validParam_returnsAffectedRows1() {
        // 1. 准备参数（为已支付订单orderId=2添加收货时间：当前时间）
        Long orderId = 5L;
        LocalDateTime receiveTime = LocalDateTime.now();

        // 2. 执行更新方法
        int affectedRows = orderMapper.updateReceiveInfo(orderId, receiveTime);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新收货时间应影响1行数据");

        // 4. 验证收货时间已更新
        Order updatedOrder = orderMapper.selectById(orderId);
        assertNotNull(updatedOrder.getReceiveTime(), "收货时间不应为null");
        assertEquals(receiveTime.toLocalDate(), updatedOrder.getReceiveTime().toLocalDate(), "收货时间应完全匹配");
    }

    /**
     * 测试selectTimeoutPendingOrders：查询超时未支付订单（正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - selectTimeoutPendingOrders方法
     */
    @Test
    void selectTimeoutPendingOrders_validTime_returnsTimeoutList() {
        // 1. 准备参数（支付过期时间设为2024-01-07 16:00:00，待支付订单orderId=1的超时时间为15:00:00，会被筛选出）
        LocalDateTime payExpireTime = LocalDateTime.of(2024, 1, 7, 16, 0, 0);

        // 2. 执行测试方法
        List<Order> timeoutList = orderMapper.selectTimeoutPendingOrders(payExpireTime);

        // 3. 断言结果（data-order.sql中仅orderId=1为超时未支付订单）
        assertNotNull(timeoutList);
        assertEquals(1, timeoutList.size(), "超时未支付订单数量应为1");
        assertEquals(pendingPayOrder.getOrderId(), timeoutList.get(0).getOrderId());
        assertEquals(OrderStatusEnum.PENDING_PAYMENT, timeoutList.get(0).getStatus(), "订单状态应为待支付");
    }

    /**
     * 测试selectByQuery：复杂条件查询订单（DTO参数，正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - selectByQuery方法
     */
    @Test
    void selectByQuery_complexCondition_returnsMatchedList() {
        // 1. 构建查询DTO（筛选：buyerId=1，status=PAID，支付方式=WECHAT；分页：第1页，每页10条）
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setBuyerId(1L);
        queryDTO.setStatus(OrderStatusEnum.PENDING_SHIPMENT);
        queryDTO.setPayType(PayTypeEnum.WECHAT_PAY);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setOffset((queryDTO.getPageNum() - 1) * queryDTO.getPageSize());

        // 2. 执行查询方法
        List<Order> matchedList = orderMapper.selectByQuery(queryDTO);

        // 3. 断言结果（data-order.sql中仅orderId=2符合所有条件）
        assertNotNull(matchedList);
        assertEquals(1, matchedList.size(), "复杂条件查询应返回1个匹配订单");
        assertEquals(paidUnshippedOrder.getOrderId(), matchedList.get(0).getOrderId());
    }

    /**
     * 测试countByBuyerId：统计买家指定状态的订单数（正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - countByBuyerId方法
     */
    @Test
    void countByBuyerId_validStatus_returnsCorrectCount() {
        // 1. 执行统计方法（统计test_buyer的已完成订单数）
        int count = orderMapper.countByBuyerId(1L, OrderStatusEnum.COMPLETED);

        // 2. 断言结果（data-order.sql中买家仅orderId=3为已完成订单，总数应为1）
        assertEquals(1, count, "买家已完成订单总数应为1");
    }

    /**
     * 测试countBySellerId：统计卖家指定状态的订单数（正常场景）
     * 适配《代码文档1》2.5.2节 条件查询与统计 - countBySellerId方法
     */
    @Test
    void countBySellerId_validStatus_returnsCorrectCount() {
        // 1. 执行统计方法（统计test_seller的待支付订单数）
        int count = orderMapper.countBySellerId(2L, OrderStatusEnum.PENDING_PAYMENT);

        // 2. 断言结果（data-order.sql中卖家仅orderId=1为待支付订单，总数应为1）
        assertEquals(1, count, "卖家待支付订单总数应为1");
    }
}