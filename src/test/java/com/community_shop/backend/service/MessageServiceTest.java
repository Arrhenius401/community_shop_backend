package com.community_shop.backend.service;

import com.community_shop.backend.convert.MessageConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.message.*;
import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.MessageMapper;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageServiceTest {

    // 模拟依赖组件
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private UserService userService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private MessageConvert messageConvert;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    // 注入测试目标服务
    @InjectMocks
    private MessageServiceImpl messageService;

    // 测试数据
    private User testSender;       // 发送者（普通用户）
    private User testReceiver;     // 接收者（普通用户）
    private User testSystemUser;   // 系统用户（模拟发送者ID=0）
    private Message testMessage;   // 测试消息实体
    private MessageSendDTO testSendDTO;           // 消息发送DTO
    private MessageStatusUpdateDTO testStatusDTO;// 消息状态更新DTO
    private MessageQueryDTO testMessageQueryDTO; // 消息列表查询DTO
    private PrivateMessageQueryDTO testPrivateQueryDTO; // 私聊查询DTO

    @BeforeEach
    void setUp() {
        // 1. 初始化测试数据
        initTestUsers();
        initTestMessage();
        initTestDTOs();

        // 2. 注入MyBatis-Plus父类baseMapper（解决ServiceImpl继承问题）
        injectBaseMapper();

        // 3. 模拟Redis依赖行为（避免doNothing()错误，非void方法用when().thenReturn()）
        mockRedisBehavior();
    }

    /**
     * 初始化测试用户数据
     */
    private void initTestUsers() {
        // 普通发送者（ID=1）
        testSender = new User();
        testSender.setUserId(1L);
        testSender.setUsername("senderUser");
        testSender.setAvatarUrl("https://sender-avatar.jpg");

        // 普通接收者（ID=2）
        testReceiver = new User();
        testReceiver.setUserId(2L);
        testReceiver.setUsername("receiverUser");
        testReceiver.setAvatarUrl("https://receiver-avatar.jpg");

        // 系统用户（模拟发送者ID=0）
        testSystemUser = new User();
        testSystemUser.setUserId(0L);
        testSystemUser.setUsername("系统通知");
        testSystemUser.setAvatarUrl("/static/avatar/system_default.png");
    }

    /**
     * 初始化测试消息实体
     */
    private void initTestMessage() {
        testMessage = new Message();
        testMessage.setMsgId(1001L);          // 消息ID
        testMessage.setSenderId(1L);           // 发送者ID（普通用户）
        testMessage.setReceiverId(2L);         // 接收者ID
        testMessage.setType(MessageTypeEnum.ORDER); // 消息类型（订单通知）
        testMessage.setContent("您的订单已支付成功，订单号：ORDER123456");
        testMessage.setOrderId(123456L);      // 关联业务ID（订单ID）
        testMessage.setIsRead(false); // 初始状态：未读
        testMessage.setCreateTime(LocalDateTime.now().minusHours(1));
        testMessage.setUpdateTime(LocalDateTime.now().minusHours(1));
    }

    /**
     * 初始化测试DTO数据
     */
    private void initTestDTOs() {
        // 1. 消息发送DTO（订单通知，接收者ID=2）
        testSendDTO = new MessageSendDTO();
        testSendDTO.setReceiverId(2L);
        testSendDTO.setType(MessageTypeEnum.ORDER);
        testSendDTO.setContent("您的订单已支付成功，订单号：ORDER123456");
        testSendDTO.setBusinessId(123456L); // 关联订单ID

        // 2. 消息状态更新DTO（标记未读→已读）
        testStatusDTO = new MessageStatusUpdateDTO();
        testStatusDTO.setMessageId(1001L);
        testStatusDTO.setTargetStatus(MessageStatusEnum.READ);

        // 3. 消息列表查询DTO（查询用户的未读订单消息）
        testMessageQueryDTO = new MessageQueryDTO();
        testMessageQueryDTO.setIsRead(false);
        testMessageQueryDTO.setType(MessageTypeEnum.ORDER);
        testMessageQueryDTO.setPageNum(1);
        testMessageQueryDTO.setPageSize(10);

        // 4. 私聊查询DTO（用户1与用户2的私聊）
        testPrivateQueryDTO = new PrivateMessageQueryDTO();
        testPrivateQueryDTO.setUserId(1L);       // 当前用户ID
        testPrivateQueryDTO.setPartnerId(2L);    // 聊天对象ID
        testPrivateQueryDTO.setPageNum(1);
        testPrivateQueryDTO.setPageSize(10);
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
            // 为测试服务注入mock的messageMapper
            baseMapperField.set(messageService, messageMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("初始化MessageService baseMapper失败", e);
        }
    }

    /**
     * 模拟Redis相关行为（避免doNothing()错误，严格区分void/非void方法）
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
     * 测试1：发送业务消息（订单通知）- 成功场景
     * 校验点：参数校验通过、接收人存在、数据库插入成功、未读缓存更新
     */
    @Test
    void testSendMessage_Success() {
        // 1. 模拟依赖行为
        when(userService.getById(1L)).thenReturn(testSender);          // 校验发送者存在
        when(userService.getById(2L)).thenReturn(testReceiver);        // 校验接收者存在

        // 使用ArgumentCaptor捕获插入的Message对象
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        // 模拟插入成功，并在捕获后手动设置主键
        when(messageMapper.insert(messageCaptor.capture())).thenAnswer(invocation -> {
            Message capturedMessage = messageCaptor.getValue();
            // 手动设置主键（模拟数据库自增）
            capturedMessage.setMsgId(1001L); // 自定义一个非空的消息ID
            return 1; // 返回插入成功的行数
        });

        when(messageConvert.messageToMessageDetailDTO(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            MessageDetailDTO dto = new MessageDetailDTO();
            dto.setMessageId(message.getMsgId());
            dto.setContent(message.getContent());
            dto.setIsRead(message.getIsRead());
            return dto;
        });

        // 2. 执行测试方法（发送者ID=1，消息参数=testSendDTO）
        Long messageId = messageService.sendMessage(1L, testSendDTO);

        // 3. 验证结果（消息ID非空，说明发送成功）
        assertNotNull(messageId);

        // 4. 验证依赖调用
        verify(userService, times(1)).getById(1L);          // 校验发送者
        verify(userService, times(1)).getById(2L);          // 校验接收者
        verify(messageMapper, times(1)).insert(any(Message.class)); // 插入消息
        verify(redisTemplate, times(1)).opsForValue();     // 更新未读缓存
    }

    /**
     * 测试2：发送业务消息 - 失败场景（接收人不存在）
     * 校验点：抛出RECEIVER_NOT_EXISTS异常，未执行数据库插入
     */
    @Test
    void testSendMessage_Fail_ReceiverNotExists() {
        // 1. 模拟依赖行为（发送者存在，接收者不存在）
        when(userService.getById(1L)).thenReturn(testSender);
        when(userService.getById(2L)).thenReturn(null); // 接收者不存在

        // 2. 执行测试并捕获异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.sendMessage(1L, testSendDTO);
        });

        // 3. 验证异常信息
        assertEquals(ErrorCode.RECEIVER_NOT_EXISTS.getCode(), exception.getCode());

        // 4. 验证依赖调用（未执行数据库插入）
        verify(messageMapper, never()).insert(any(Message.class));
    }

    /**
     * 测试3：获取消息详情 - 成功场景
     * 校验点：消息存在、操作人是接收者、DTO封装正确
     */
    @Test
    void testGetMessageDetail_Success() {
        // 1. 模拟依赖行为
        when(userService.getById(2L)).thenReturn(testReceiver); // 接收者存在
        when(messageMapper.selectById(1001L)).thenReturn(testMessage); // 消息存在
        when(messageConvert.messageToMessageDetailDTO(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            MessageDetailDTO dto = new MessageDetailDTO();
            dto.setMessageId(message.getMsgId());
            dto.setContent(message.getContent());
            dto.setIsRead(message.getIsRead());
            // 封装发送者信息
            MessageDetailDTO.SenderDTO senderDTO = new MessageDetailDTO.SenderDTO();
            senderDTO.setUserId(message.getSenderId());
            senderDTO.setUsername(testSender.getUsername());
            senderDTO.setAvatarUrl(testSender.getAvatarUrl());
            dto.setSender(senderDTO);
            return dto;
        });
        when(userService.getById(1L)).thenReturn(testSender); // 接收者存在

        // 2. 执行测试方法（接收者ID=2，消息ID=1001）
        MessageDetailDTO result = messageService.getMessageDetail(2L, 1001L);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getMessageId());
        assertEquals(testMessage.getContent(), result.getContent());
        assertEquals(testSender.getUsername(), result.getSender().getUsername());

        // 4. 验证依赖调用
        verify(messageMapper, times(1)).selectById(1001L);
        verify(messageConvert, times(1)).messageToMessageDetailDTO(any(Message.class));
    }

    /**
     * 测试4：更新消息状态（未读→已读）- 成功场景
     * 校验点：消息归属正确、状态流转合法、未读缓存更新
     */
    @Test
    void testUpdateMessageStatus_Success_UnreadToRead() {
        // 1. 模拟依赖行为
        when(messageMapper.selectById(1001L)).thenReturn(testMessage); // 消息存在
        when(messageMapper.updateDeleteStatus(anyLong(), anyBoolean())).thenReturn(1); // 更新成功
        when(messageMapper.updateReadStatus(anyLong(), anyBoolean())).thenReturn(1); // 更新成功
        // 模拟未读统计缓存存在
        MessageStatDTO cachedStat = new MessageStatDTO();
        cachedStat.setTotalUnread(5); // 原未读数5
        when(valueOperations.get("message:unread:stat:2")).thenReturn(cachedStat);

        // 2. 执行测试方法（接收者ID=2，更新状态为已读）
        Boolean result = messageService.updateMessageStatus(2L, testStatusDTO);

        // 3. 验证结果
        assertTrue(result);

        // 4. 验证依赖调用
        verify(messageMapper, times(1)).selectById(1001L); // 查询消息
        verify(messageMapper, times(1)).updateReadStatus(anyLong(), anyBoolean()); // 更新状态
        verify(redisTemplate, times(1)).delete("message:recent:unread:2"); // 清除最近未读缓存
        verify(valueOperations, times(1)).set(anyString(), any(MessageStatDTO.class), eq(5L), eq(TimeUnit.MINUTES)); // 更新未读统计
    }

    /**
     * 测试5：更新消息状态 - 失败场景（非接收人操作，无权限）
     * 校验点：抛出PERMISSION_DENIED异常，未执行更新
     */
    @Test
    void testUpdateMessageStatus_Fail_NoPermission() {
        // 1. 模拟依赖行为（消息存在，但操作人不是接收者）
        when(messageMapper.selectById(1001L)).thenReturn(testMessage);
        when(userService.getById(3L)).thenReturn(new User()); // 操作人ID=3（非接收者）

        // 2. 执行测试并捕获异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.updateMessageStatus(3L, testStatusDTO); // 操作人ID=3，接收者ID=2
        });

        // 3. 验证异常信息
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), exception.getCode());

        // 4. 验证依赖调用（未执行更新）
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    /**
     * 测试6：分页查询消息列表 - 成功场景（从数据库查询）
     * 校验点：分页参数正确、DTO转换正确、总条数匹配
     */
    @Test
    void testSearchMessagesByQuery_Success_FromDb() {
        // 1. 准备测试数据
        List<Message> messageList = Arrays.asList(testMessage);
        MessageListItemDTO listItemDTO = new MessageListItemDTO();
        listItemDTO.setMessageId(1001L);
        listItemDTO.setContentSummary("您的订单已支付成功，订单号：ORDER123456");
        listItemDTO.setType(MessageTypeEnum.ORDER);
        listItemDTO.setStatus(MessageStatusEnum.UNREAD);

        // 2. 模拟依赖行为
        when(messageMapper.countByQuery(testMessageQueryDTO)).thenReturn(1); // 总条数=1
        when(messageMapper.selectByQuery(testMessageQueryDTO)).thenReturn(messageList); // 消息列表
        when(messageConvert.messageToMessageListItemDTO(any(Message.class))).thenReturn(listItemDTO);

        // 3. 执行测试方法（用户ID=2，查询未读订单消息）
        PageResult<MessageListItemDTO> result = messageService.searchMessagesByQuery(2L, testMessageQueryDTO);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal()); // 总条数=1
        assertEquals(1, result.getList().size()); // 列表长度=1
        assertEquals(1001L, result.getList().get(0).getMessageId());

        // 5. 验证依赖调用
        verify(messageMapper, times(1)).countByQuery(testMessageQueryDTO);
        verify(messageMapper, times(1)).selectByQuery(testMessageQueryDTO);
    }

    /**
     * 测试7：获取最近未读消息预览 - 成功场景（缓存未命中，从数据库查询）
     * 校验点：最多返回3条、内容摘要截取正确、缓存更新
     */
    @Test
    void testGetRecentUnreadPreviews_Success_FromDb() {
        // 1. 准备测试数据（3条未读消息）
        Message unreadMsg1 = new Message();
        unreadMsg1.setMsgId(1002L);
        unreadMsg1.setSenderId(0L); // 系统发送
        unreadMsg1.setType(MessageTypeEnum.SYSTEM);
        unreadMsg1.setContent("系统公告：社区将于2024年12月31日进行维护");
        unreadMsg1.setCreateTime(LocalDateTime.now().minusMinutes(30));

        List<Message> recentUnreadList = Arrays.asList(testMessage, unreadMsg1);

        // 2. 模拟依赖行为
        when(messageMapper.selectRecentUnreadByUser(2L, 3)).thenReturn(recentUnreadList); // 查询3条未读
        when(userService.getById(1L)).thenReturn(testSender); // 普通发送者信息

        // 3. 执行测试方法（用户ID=2）
        List<MessagePreviewDTO> result = messageService.getRecentUnreadPreviews(2L);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(2, result.size()); // 实际返回2条
        assertEquals("系统通知", result.get(1).getSenderName()); // 系统消息发送者名称正确
        assertEquals("系统公告：社区将于2024年12月31日进行维护", result.get(1).getContentSummary()); // 内容未截取

        // 5. 验证依赖调用
        verify(messageMapper, times(1)).selectRecentUnreadByUser(2L, 3);
        // 修正：预期调用2次 opsForValue()（一次get，一次set）
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations, times(1)).get(anyString()); // 验证缓存查询
        verify(valueOperations, times(1)).set(
                eq("message:recent:unread:2"),
                anyList(),
                eq(3L),
                eq(TimeUnit.MINUTES)
        ); // 验证缓存更新
    }

    /**
     * 测试8：统计未读消息数 - 成功场景
     * 校验点：返回正确未读数量、调用Mapper统计方法
     */
    @Test
    void testCountUnreadMessages_Success() {
        // 1. 模拟依赖行为（用户2有3条未读消息）
        when(messageMapper.countUnread(2L)).thenReturn(3);

        // 2. 执行测试方法
        Integer unreadCount = messageService.countUnreadMessages(2L);

        // 3. 验证结果
        assertNotNull(unreadCount);
        assertEquals(3, unreadCount);

        // 4. 验证依赖调用
        verify(messageMapper, times(1)).countUnread(2L);
    }
}