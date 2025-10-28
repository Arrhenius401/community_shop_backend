package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.dto.message.MessageQueryDTO;
import com.community_shop.backend.dto.message.PrivateMessageQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.7节 MessageMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.5节 Message实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 * 4. 《测试文档1 基础SQL脚本设计.docx》MESSAGE模块初始化数据
 */
@MybatisPlusTest  // 仅加载MyBatis相关Bean，轻量化测试
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（加载application-test.properties）
public class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;  // 注入待测试的MessageMapper

    // 测试复用的基础数据（从data-message.sql初始化数据中获取）
    private Message systemMessage;       // 系统消息（msgId=1，type=SYSTEM，status=UNREAD）
    private Message orderUnreadMessage;  // 订单未读消息（msgId=2，type=ORDER，status=UNREAD）
    private Message deletedMessage;      // 已删除消息（msgId=3，type=ORDER，status=DELETED）
    private Message readMessage;         // 已读消息（msgId=4，type=ORDER，status=READ）

    /**
     * 测试前初始化：从数据库查询基础测试消息，确保与data-message.sql数据一致
     * 适配《代码文档0》中Message实体的枚举属性（type/status）与业务属性（isRead/isDeleted）
     */
    @BeforeEach
    void setUp() {
        // 按msgId查询（基于BaseMapper的selectById方法）
        systemMessage = messageMapper.selectById(1L);
        orderUnreadMessage = messageMapper.selectById(2L);
        deletedMessage = messageMapper.selectById(3L);
        readMessage = messageMapper.selectById(4L);

        // 断言初始化成功（确保MESSAGE模块SQL脚本已正确执行）
        assertNotNull(systemMessage, "初始化失败：未查询到系统消息（data-message.sql中msgId=1）");
        assertNotNull(orderUnreadMessage, "初始化失败：未查询到订单未读消息（data-message.sql中msgId=2）");
        assertNotNull(deletedMessage, "初始化失败：未查询到已删除消息（data-message.sql中msgId=3）");
        assertNotNull(readMessage, "初始化失败：未查询到已读消息（data-message.sql中msgId=4）");
    }

    /**
     * 测试selectByReceiver：按接收人+类型查询消息（正常场景，指定类型）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectByReceiver方法
     */
    @Test
    void selectByReceiver_specifyType_returnsMessageList() {
        // 1. 执行测试方法（查询test_buyer（receiverId=1）的订单类型消息，分页：offset=0，limit=10）
        List<Message> orderMessageList = messageMapper.selectByReceiver(1L, MessageTypeEnum.ORDER, 0, 10);

        // 2. 断言结果（data-message.sql中receiverId=1的订单类型消息有3条：msgId=2、3、4）
        assertNotNull(orderMessageList);
        assertEquals(3, orderMessageList.size(), "用户receiverId=1的订单类型消息应有3条");

        // 验证消息类型与接收人匹配
        boolean allOrderType = orderMessageList.stream()
                .allMatch(msg -> MessageTypeEnum.ORDER.equals(msg.getType()));
        boolean allReceiver1 = orderMessageList.stream()
                .allMatch(msg -> msg.getReceiverId().equals(1L));
        assertTrue(allOrderType, "查询结果应均为订单类型消息（type=ORDER）");
        assertTrue(allReceiver1, "查询结果的接收人应均为receiverId=1");
    }

    /**
     * 测试selectByReceiver：按接收人查询所有类型消息（正常场景，不指定类型）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectByReceiver方法
     */
    @Test
    void selectByReceiver_allType_returnsAllMessage() {
        // 1. 执行测试方法（查询test_buyer（receiverId=1）的所有类型消息，分页：offset=0，limit=10）
        List<Message> allMessageList = messageMapper.selectByReceiver(1L, null, 0, 10);

        // 2. 断言结果（data-message.sql中receiverId=1的消息共3条，系统消息receiverId=-1不包含）
        assertNotNull(allMessageList);
        assertEquals(5, allMessageList.size(), "用户receiverId=1的所有类型消息应有5条");
    }

    /**
     * 测试countUnread：统计接收人的未读消息数（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - countUnread方法
     */
    @Test
    void countUnread_validReceiver_returnsCorrectCount() {
        // 1. 执行测试方法（统计test_buyer（receiverId=1）的未读消息数）
        int unreadCount = messageMapper.countUnread(1L);

        // 2. 断言结果（data-message.sql中receiverId=1的未读消息仅msgId=2，总数应为1）
        assertEquals(2, unreadCount, "用户receiverId=1的未读消息数应为2");
    }

    /**
     * 测试countUnreadByType：按类型统计未读消息数（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - countUnreadByType方法
     */
    @Test
    void countUnreadByType_specifyType_returnsCorrectCount() {
        // 1. 执行测试方法（统计test_buyer（receiverId=1）的订单类型未读消息数）
        int orderUnreadCount = messageMapper.countUnreadByType(1L, MessageTypeEnum.ORDER);

        // 2. 断言结果（data-message.sql中receiverId=1的订单类型未读消息仅msgId=2，总数应为1）
        assertEquals(1, orderUnreadCount, "用户receiverId=1的订单类型未读消息数应为1");
    }

    /**
     * 测试updateReadStatus：更新消息阅读状态（正常场景，标记为已读）
     * 适配《代码文档1》2.7.2节 状态更新 - updateReadStatus方法
     * 适配《中间件文档3》枚举TypeHandler自动转换（枚举→数据库code）
     */
    @Test
    void updateReadStatus_markAsRead_returnsAffectedRows1() {
        // 1. 准备参数（将未读消息msgId=2标记为已读，isRead=1）
        Long msgId = 2L;
        boolean isRead = true;

        // 2. 执行更新方法
        int affectedRows = messageMapper.updateReadStatus(msgId, isRead);

        // 3. 断言更新行数
        assertEquals(1, affectedRows, "更新消息阅读状态应影响1行数据");

        // 4. 验证状态已更新（查询结果自动转换为枚举）
        Message updatedMessage = messageMapper.selectById(msgId);
        assertTrue(updatedMessage.getIsRead(), "消息应更新为已读状态（isRead=true）");
    }

    /**
     * 测试batchUpdateReadStatus：批量更新阅读状态（正常场景，批量标记为已读）
     * 适配《代码文档1》2.7.2节 状态更新 - batchUpdateReadStatus方法
     */
    @Test
    void batchUpdateReadStatus_batchMarkAsRead_returnsAffectedRows2() {
        // 1. 准备参数（批量将msgId=2和msgId=4标记为已读，isRead=1）
        List<Long> msgIds = List.of(2L, 4L);
        boolean isRead = true;

        // 2. 执行批量更新方法
        int affectedRows = messageMapper.batchUpdateReadStatus(msgIds, isRead);

        // 3. 断言更新行数
        assertEquals(2, affectedRows, "批量更新阅读状态应影响2行数据");

        // 4. 验证批量更新结果
        Message msg2 = messageMapper.selectById(2L);
        Message msg4 = messageMapper.selectById(4L);
        assertTrue(msg2.getIsRead(), "msgId=2应更新为已读状态（isRead=true）");
        assertTrue(msg4.getIsRead(), "msgId=4应更新为已读状态（isRead=true）");
    }

    /**
     * 测试countByQuery：统计复杂条件下的消息总数（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - countByQuery方法
     */
    @Test
    void countByQuery_complexCondition_returnsCorrectCount() {
        // 1. 构建查询DTO（筛选：receiverId=1、type=ORDER、status=UNREAD）
        MessageQueryDTO queryDTO = new MessageQueryDTO();
        queryDTO.setReceiverId(1L);
        queryDTO.setType(MessageTypeEnum.ORDER);
        queryDTO.setIsRead(false);

        // 2. 执行统计方法
        int messageCount = messageMapper.countByQuery(queryDTO);

        // 3. 断言结果（data-message.sql中仅msgId=2符合条件，总数应为1）
        assertEquals(1, messageCount, "符合条件（receiverId=1、ORDER类型、UNREAD状态）的消息总数应为1");
    }

    /**
     * 测试selectByQuery：按条件查询消息（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectByQuery方法
     */
    @Test
    void selectByQuery_normalScenario_returnsCorrectMessages() {
        // 1. 构建查询DTO（筛选：receiverId=1、type=ORDER、status=UNREAD）
        MessageQueryDTO queryDTO = new MessageQueryDTO();
        queryDTO.setReceiverId(1L);
        queryDTO.setType(MessageTypeEnum.ORDER);
        queryDTO.setIsRead(false);

        // 2. 执行查询方法
        List<Message> messageList = messageMapper.selectByQuery(queryDTO);

        // 3. 断言结果（data-message.sql中仅msgId=2符合条件，总数应为1）
        assertEquals(1, messageList.size(), "符合条件（receiverId=1、ORDER类型、UNREAD状态）的消息总数应为1");
    }

    /**
     * 测试countByPrivateQuery：按条件查询消息（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectByQuery方法
     */
    @Test
    void countByPrivateQuery_normalScenario_returnsCorrectCount() {
        // 1. 构建查询DTO（筛选：userId=1、partnerId=2和type=PRIVATE）
        PrivateMessageQueryDTO queryDTO = new PrivateMessageQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setPartnerId(2L);
        queryDTO.setType(MessageTypeEnum.PRIVATE);

        // 2. 执行查询方法
        int messageCount = messageMapper.countByPrivateQuery(queryDTO);

        // 3. 断言结果（data-message.sql中仅msgId=2符合条件，总数应为1）
        assertEquals(2, messageCount, "符合条件（userId=1、partnerId=2和type=PRIVATE类型）的消息总数应为2");
    }

    /**
     * 测试selectByPrivateQuery：按条件查询消息（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectByQuery方法
     */
    @Test
    void selectByPrivateQuery_normalScenario_returnsCorrectMessages() {
        // 1. 构建查询DTO（筛选：userId=1、partnerId=2和type=PRIVATE）
        PrivateMessageQueryDTO queryDTO = new PrivateMessageQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setPartnerId(2L);
        queryDTO.setType(MessageTypeEnum.PRIVATE);

        // 2. 执行查询方法
        List<Message> messageList = messageMapper.selectByPrivateQuery(queryDTO);

        // 3. 断言结果（data-message.sql中仅msgId=2符合条件，总数应为1）
        assertEquals(2, messageList.size(), "符合条件（userId=1、partnerId=2和type=PRIVATE类型）的消息总数应为1");
    }

    /**
     * 测试selectRecentUnreadByUser：查询用户最近未读消息（正常场景）
     * 适配《代码文档1》2.7.2节 查询与统计 - selectRecentUnreadByUser方法
     */
    @Test
    void selectRecentUnreadByUser_validMaxCount_returnsRecentUnread() {
        // 1. 执行测试方法（查询test_buyer（userId=1）最近5条未读消息）
        List<Message> recentUnreadList = messageMapper.selectRecentUnreadByUser(1L, 5);

        // 2. 断言结果（data-message.sql中userId=1的未读消息仅msgId=2，总数应为1）
        assertNotNull(recentUnreadList);
        assertEquals(2, recentUnreadList.size(), "用户userId=1的最近未读消息数应为2");

        // 验证消息属性
        Message recentUnread = recentUnreadList.get(0);
        assertEquals(3L, recentUnread.getMsgId(), "最近未读消息应为msgId=2");
        assertFalse(recentUnread.getIsRead(), "消息状态应为UNREAD");
    }

    /**
     * 测试insert：新增消息（正常场景，新增订单消息）
     * 适配《代码文档1》2.7.2节 查询与统计 - insert方法
     */
    @Test
    void insert_newOrderMessage_returnsAffectedRows1() {
        // 1. 构建新增消息对象（新增test_buyer（receiverId=1）的订单消息）
        Message newMessage = new Message();
        newMessage.setSenderId(0L);  // 系统发送
        newMessage.setReceiverId(1L);
        newMessage.setTitle("订单发货通知");
        newMessage.setContent("您的订单ORDER20240109001已发货");
        newMessage.setOrderId(5L);  // 关联待收货订单orderId=5
        newMessage.setIsRead(false);
        newMessage.setIsDeleted(false);
        newMessage.setType(MessageTypeEnum.ORDER);
        newMessage.setCreateTime(LocalDateTime.now());

        // 2. 执行新增方法
        int affectedRows = messageMapper.insert(newMessage);

        // 3. 断言新增行数
        assertEquals(1, affectedRows, "新增消息应影响1行数据");

        // 4. 验证新增结果（通过msgId查询）
        Message insertedMessage = messageMapper.selectById(newMessage.getMsgId());
        assertNotNull(insertedMessage, "未查询到新增的消息");
        assertEquals("订单发货通知", insertedMessage.getTitle(), "新增消息标题不匹配");
        assertEquals(MessageTypeEnum.ORDER, insertedMessage.getType(), "新增消息类型应为ORDER");
    }
}