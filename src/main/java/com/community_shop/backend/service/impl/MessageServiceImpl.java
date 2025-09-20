package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.MessageConvert;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.message.*;
import com.community_shop.backend.enums.CodeEnum.MessageStatusEnum;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.mapper.MessageMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现类，基于MyBatis操作message表，实现消息发送与管理功能
 */
@Slf4j
@Service
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, Message> implements MessageService {

    // 缓存相关常量
    private static final String CACHE_KEY_UNREAD_STAT = "message:unread:stat:"; // 未读统计缓存Key前缀
    private static final String CACHE_KEY_RECENT_UNREAD = "message:recent:unread:"; // 最近未读预览缓存Key前缀
    private static final long CACHE_TTL_UNREAD_STAT = 5; // 未读统计缓存有效期（分钟）
    private static final long CACHE_TTL_RECENT_UNREAD = 3; // 最近未读预览缓存有效期（分钟）
    private static final int MAX_RECENT_UNREAD_COUNT = 3; // 最近未读消息预览最大条数
    private static final int MAX_MESSAGE_CONTENT_LENGTH = 1000; // 消息内容最大长度（字符）
    private static final int MAX_NOTICE_TITLE_LENGTH = 50; // 系统公告标题最大长度（字符）

    @Autowired
    private MessageMapper messageMapper;

    @Resource
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MessageConvert messageConvert;

    /**
     * 发送业务触发消息（如订单支付通知、评价提醒）
     *
     * @param messageSendDTO 消息参数（接收人、内容、类型、关联业务ID）
     * @return 消息ID
     * @throws BusinessException 接收人不存在、内容超限时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long userId, MessageSendDTO messageSendDTO) {
        try {
            // 1. 参数校验
            validateBusinessMessageParam(messageSendDTO);
            User sender = userService.getById(userId);
            if (Objects.isNull(sender)) {
                log.error("发送业务消息失败，发送人不存在，发送人ID：{}", userId);
                throw new BusinessException(ErrorCode.SENDER_NOT_EXISTS);
            }

            // 2. 校验接收人存在
            User receiver = userService.getById(messageSendDTO.getReceiverId());
            if (Objects.isNull(receiver)) {
                log.error("发送业务消息失败，接收人不存在，接收人ID：{}", messageSendDTO.getReceiverId());
                throw new BusinessException(ErrorCode.RECEIVER_NOT_EXISTS);
            }

            // 3. 构建Message实体
            Message message = buildBusinessMessage(messageSendDTO);
            message.setSenderId(userId);

            // 4. 插入数据库
            int insertRows = messageMapper.insert(message);
            if (insertRows <= 0) {
                log.error("发送业务消息失败，数据库插入失败，消息参数：{}", messageSendDTO);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 5. 更新未读消息缓存（+1）
            updateUnreadStatCache(messageSendDTO.getReceiverId(), 1);

            log.info("发送业务消息成功，消息ID：{}，接收人ID：{}，消息类型：{}",
                    message.getMsgId(), messageSendDTO.getReceiverId(), messageSendDTO.getType());
            return message.getMsgId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送业务消息异常，消息参数：{}", messageSendDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 获取消息详情
     *
     * @param userId 用户ID
     * @param msgId 消息ID
     * @return 消息详情
     * @throws BusinessException 无权限（非接收人）时抛出
     */
    @Override
    public MessageDetailDTO getMessageDetail(Long userId, Long msgId) {
        try {
            // 1. 参数检验
            User receiver = userService.getById(userId);
            if (Objects.isNull(receiver)) {
                log.error("获取消息详情失败，用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            Message message= messageMapper.selectById(msgId);
            if (Objects.isNull(message)) {
                log.error("获取消息详情失败，消息不存在，消息ID：{}", msgId);
                throw new BusinessException(ErrorCode.MESSAGE_NOT_EXISTS);
            }

            // 2. 封装为DTO形式
            MessageDetailDTO messageDetailDTO = messageConvert.messageToMessageDetailDTO(message);
            messageDetailDTO.setReceiver(new MessageDetailDTO.ReceiverDTO(
                    message.getReceiverId(), receiver.getUsername())
            );
            User sender = userService.getById(message.getSenderId());
            if(sender == null){
                messageDetailDTO.setSender(new MessageDetailDTO.SenderDTO(
                        message.getSenderId(), "未知", null)
                );
            }
            messageDetailDTO.setSender(new MessageDetailDTO.SenderDTO(
                    message.getSenderId(), sender.getUsername(), sender.getAvatarUrl())
            );

            return messageDetailDTO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取消息详情异常，用户ID：{}，消息ID：{}", userId, msgId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 标记消息状态（已读/删除）
     *
     * @param userId 用户ID
     * @param statusUpdateDTO 状态更新参数（消息ID、目标状态、操作人）
     * @return 是否更新成功
     * @throws BusinessException 无权限（非接收人）时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMessageStatus(Long userId, MessageStatusUpdateDTO statusUpdateDTO) {
        try {
            // 1. 参数校验
            if (Objects.isNull(statusUpdateDTO) || Objects.isNull(statusUpdateDTO.getMessageId())
                    || Objects.isNull(statusUpdateDTO.getTargetStatus()) || Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验消息存在且操作人是接收人
            Message message = messageMapper.selectById(statusUpdateDTO.getMessageId());
            validateMessageOwner(message, userId);

            // 3. 校验状态流转合法性（仅允许未读→已读、已读→删除、未读→删除）
            validateStatusTransition(message.getStatus(), statusUpdateDTO.getTargetStatus());

            // 4. 执行状态更新
            Message updateMessage = new Message();
            updateMessage.setMsgId(statusUpdateDTO.getMessageId());
            updateMessage.setStatus(statusUpdateDTO.getTargetStatus());
            updateMessage.setUpdateTime(LocalDateTime.now());
            int updateRows = messageMapper.updateById(updateMessage);

            if (updateRows > 0) {
                // 5. 若从“未读”转为其他状态，更新未读缓存（-1）
                if (MessageStatusEnum.UNREAD.equals(message.getStatus())) {
                    updateUnreadStatCache(userId, -1);
                }
                // 6. 清除相关缓存（最近未读预览）
                clearRecentUnreadCache(userId);
            }

            log.info("更新消息状态成功，消息ID：{}，操作人ID：{}，原状态：{}，目标状态：{}，成功条数：{}",
                    statusUpdateDTO.getMessageId(), userId,
                    message.getStatus(), statusUpdateDTO.getTargetStatus(), updateRows);
            return updateRows > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新消息状态异常，参数：{}", statusUpdateDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 分页查询用户消息列表
     *
     * @param messageQueryDTO 查询参数（用户ID、类型、状态、分页）
     * @return 分页消息列表
     */
    @Override
    public PageResult<MessageListItemDTO> searchMessagesByQuery(Long userId, MessageQueryDTO messageQueryDTO) {
        try {
            // 1. 参数校验
            if (Objects.isNull(messageQueryDTO) || Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }
            // 处理默认分页参数（pageNum默认1，pageSize默认10）
            int pageNum = Objects.nonNull(messageQueryDTO.getPageNum()) ? messageQueryDTO.getPageNum() : 1;
            int pageSize = Objects.nonNull(messageQueryDTO.getPageSize()) ? messageQueryDTO.getPageSize() : 10;

            // 2. 分页查询数据库
            long total = messageMapper.countByQuery(messageQueryDTO);
            List<Message> messageList = messageMapper.selectByQuery(messageQueryDTO);
            long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

            // 3. 转换为DTO列表（脱敏发送者信息）
            List<MessageListItemDTO> dtoList = messageList.stream()
                    .map(message -> {
                        MessageListItemDTO dto = messageConvert.messageToMessageListItemDTO(message);
                        // 处理系统消息发送者信息（固定为“系统通知”）
                        if (MessageTypeEnum.SYSTEM.equals(message.getType())) {
                            dto.getSender().setUsername("系统通知");
                            dto.getSender().setAvatarUrl("/static/avatar/system_default.png"); // 系统默认头像
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            // 4. 封装分页结果
            PageResult<MessageListItemDTO> pageResult = new PageResult<>();
            pageResult.setTotal(total);
            pageResult.setTotalPages(totalPages);
            pageResult.setList(dtoList);
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);

            log.info("查询用户消息列表成功，用户ID：{}，查询条件：{}，总条数：{}，总页数：{}",
                    userId, messageQueryDTO, total, totalPages);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询用户消息列表异常，参数：{}", messageQueryDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public PageResult<PrivateMessageDetailDTO> searchPrivateMessagesByQuery(Long userId, PrivateMessageQueryDTO queryDTO) {
        try {
            // 1.参数校验
            if (Objects.isNull(queryDTO) || Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2.检验用户存在
            User partner = userService.getById(queryDTO.getPartnerId());
            if (Objects.isNull(partner)) {
                log.error("查询用户私信失败，用户不存在，用户ID：{}", userId);
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }


            // 2.分段查询数据库
            List<Message> messages = messageMapper.selectByPrivateQuery(queryDTO);
            List<PrivateMessageDetailDTO> messageDTOs = convertMessagesToDTOs(messages);
            int pageSize = messages.size();
            long total = messageMapper.countByPrivateQuery(queryDTO);
            long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

            // 封装分页结果
            // Math.ceil() 是为了处理整除不尽的情况,结果向上取整（如 137 条不能被 10 整除，需要多一页显示剩余 7 条）
            return new PageResult<>(
                    total,
                    totalPages,
                    messageDTOs,
                    queryDTO.getPageNum(),
                    queryDTO.getPageSize()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询用户私信异常，参数：{}", queryDTO, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 获取用户最近3条未读消息预览
     *
     * @param userId 用户ID
     * @return 未读消息预览列表
     */
    @Override
    public List<MessagePreviewDTO> getRecentUnreadPreviews(Long userId) {
        try {
            // 1. 参数校验
            if (Objects.isNull(userId)) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 先查缓存
            String cacheKey = CACHE_KEY_RECENT_UNREAD + userId;
            List<MessagePreviewDTO> cachedPreviews = (List<MessagePreviewDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (Objects.nonNull(cachedPreviews)) {
                log.info("从缓存获取用户最近未读预览，用户ID：{}", userId);
                return cachedPreviews;
            }

            // 3. 缓存未命中，查询数据库（按创建时间倒序，取前3条）
            List<Message> recentUnreadMessages = messageMapper.selectRecentUnreadByUser(userId, MAX_RECENT_UNREAD_COUNT);
            List<MessagePreviewDTO> previewList = new ArrayList<>();
            if (!recentUnreadMessages.isEmpty()) {
                previewList = recentUnreadMessages.stream()
                        .map(message -> {
                            MessagePreviewDTO preview = new MessagePreviewDTO();
                            preview.setMessageId(message.getMsgId());
                            preview.setType(message.getType());
                            preview.setCreateTime(message.getCreateTime());
                            // 处理内容摘要（截取前30字）
                            String content = message.getContent();
                            preview.setContentSummary(StringUtils.hasText(content)
                                    ? content.length() > 30 ? content.substring(0, 30) + "..." : content
                                    : "无内容");
                            // 处理系统消息发送者名称
                            if (MessageTypeEnum.SYSTEM.equals(message.getType())) {
                                preview.setSenderName("系统通知");
                            } else {
                                // 获取发送者信息
                                User sender = userService.getById(message.getSenderId());
                                if (Objects.nonNull(sender)) {
                                    preview.setSenderName(sender.getUsername());
                                } else {
                                    preview.setSenderName("未知用户");
                                }
                            }
                            return preview;
                        })
                        .collect(Collectors.toList());
            }

            // 4. 缓存结果
            redisTemplate.opsForValue().set(cacheKey, previewList, CACHE_TTL_RECENT_UNREAD, TimeUnit.MINUTES);

            log.info("获取用户最近未读预览成功，用户ID：{}，预览条数：{}", userId, previewList.size());
            return previewList;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户最近未读预览异常，用户ID：{}", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 校验业务消息参数合法性
     */
    private void validateBusinessMessageParam(MessageSendDTO sendDTO) {
        // 接收人ID非空
        if (Objects.isNull(sendDTO.getReceiverId())) {
            throw new BusinessException(ErrorCode.MESSAGE_RECEIVER_NULL);
        }
        // 消息类型非空
        if (Objects.isNull(sendDTO.getType())) {
            throw new BusinessException(ErrorCode.MESSAGE_TYPE_NULL);
        }
        // 内容非空且不超过最大长度
        if (!StringUtils.hasText(sendDTO.getContent())) {
            throw new BusinessException(ErrorCode.MESSAGE_CONTENT_NULL);
        }
        if (sendDTO.getContent().length() > MAX_MESSAGE_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.MESSAGE_CONTENT_INVALID);
        }
        // 订单类消息必须关联订单ID
        if (MessageTypeEnum.ORDER.equals(sendDTO.getType()) && Objects.isNull(sendDTO.getBusinessId())) {
            throw new BusinessException(ErrorCode.ORDER_ID_NULL);
        }
    }

    /**
     * 构建业务消息实体
     */
    private Message buildBusinessMessage(MessageSendDTO sendDTO) {
        Message message = new Message();
        message.setSenderId(0L); // 业务消息发送者为系统（固定0）
        message.setReceiverId(sendDTO.getReceiverId());
        message.setType(sendDTO.getType());
        message.setContent(sendDTO.getContent());
        message.setOrderId(sendDTO.getBusinessId()); // 关联业务ID（如订单ID）
        message.setStatus(MessageStatusEnum.UNREAD); // 初始状态为未读
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
//        // 处理附件（JSON格式转列表）
//        if (StringUtils.hasText(sendDTO.getAttachments())) {
//            // 实际项目中需结合JSON工具解析，此处简化处理
//            message.setAttachments(sendDTO.getAttachments());
//        }
        return message;
    }

    /**
     * 校验消息归属（操作人是否为接收人）
     */
    private void validateMessageOwner(Message message, Long operatorId) {
        if (Objects.isNull(message)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_EXISTS);
        }
        if (!message.getReceiverId().equals(operatorId)) {
            log.error("校验消息归属失败，操作人非接收人，消息ID：{}，操作人ID：{}，接收人ID：{}",
                    message.getMsgId(), operatorId, message.getReceiverId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }

    /**
     * 校验消息状态流转合法性
     */
    private void validateStatusTransition(MessageStatusEnum currentStatus, MessageStatusEnum targetStatus) {
        // 允许的流转：未读→已读、未读→删除、已读→删除
        boolean isAllowed = (MessageStatusEnum.UNREAD.equals(currentStatus) && MessageStatusEnum.READ.equals(targetStatus))
                || (MessageStatusEnum.UNREAD.equals(currentStatus) && MessageStatusEnum.DELETED.equals(targetStatus))
                || (MessageStatusEnum.READ.equals(currentStatus) && MessageStatusEnum.DELETED.equals(targetStatus));
        if (!isAllowed) {
            log.error("校验消息状态流转失败，不允许从{}转为{}", currentStatus, targetStatus);
            throw new BusinessException(ErrorCode.MESSAGE_STATUS_TRANSITION_INVALID);
        }
    }

    /**
     * 更新未读消息统计缓存
     *
     * @param userId     用户ID
     * @param changeCount 变更数量（+1：新增未读；-1：减少未读）
     */
    private void updateUnreadStatCache(Long userId, int changeCount) {
        String cacheKey = CACHE_KEY_UNREAD_STAT + userId;
        // 先尝试获取缓存并更新
        MessageStatDTO cachedStat = (MessageStatDTO) redisTemplate.opsForValue().get(cacheKey);
        if (Objects.nonNull(cachedStat)) {
            // 更新总未读
            int newTotal = Math.max(cachedStat.getTotalUnread() + changeCount, 0);
            cachedStat.setTotalUnread(newTotal);
            // 此处简化处理：实际需根据消息类型更新对应类型的未读数，需结合具体场景传递类型参数
            redisTemplate.opsForValue().set(cacheKey, cachedStat, CACHE_TTL_UNREAD_STAT, TimeUnit.MINUTES);
            log.info("更新用户未读缓存，用户ID：{}，变更数量：{}，更新后总未读：{}", userId, changeCount, newTotal);
        } else {
            // 缓存未命中，触发主动查询（后续请求会命中缓存）
            countUnreadMessages(userId);
            log.info("未读缓存未命中，触发主动查询，用户ID：{}", userId);
        }
    }

    /**
     * 清除用户最近未读预览缓存
     */
    private void clearRecentUnreadCache(Long userId) {
        String cacheKey = CACHE_KEY_RECENT_UNREAD + userId;
        redisTemplate.delete(cacheKey);
        log.info("清除用户最近未读预览缓存，用户ID：{}", userId);
    }

    /**
     * 将消息列表转换为DTO列表
     */
    private List<PrivateMessageDetailDTO> convertMessagesToDTOs(List<Message> messages) {
        return messages.stream().map(message -> {
            PrivateMessageDetailDTO dto = new PrivateMessageDetailDTO();
            dto.setMessageId(message.getMsgId());
            dto.setContent(message.getContent());
            dto.setCreateTime(message.getCreateTime());

            // 设置发送者信息
            User sender = userService.getById(message.getSenderId());
            PrivateMessageDetailDTO.SenderDTO senderDTO = new PrivateMessageDetailDTO.SenderDTO();
            senderDTO.setUserId(message.getSenderId());
            // 假设你有方法获取用户名和头像URL
             senderDTO.setUsername(sender.getUsername());
             senderDTO.setAvatarUrl(sender.getAvatarUrl());
            dto.setSender(senderDTO);

            // 如果有附件列表，也需要设置
            // dto.setAttachments(message.getAttachments());

            return dto;
        }).collect(Collectors.toList());
    }

    //==================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendSellerNotice(Long sellerId, String content, Long orderId) {
        // 校验卖家是否存在
        User user = userService.getById(sellerId);
        if (user == null) {
            throw new RuntimeException("卖家不存在");
        }

        // 创建消息实体
        Message message = new Message();
        message.setSenderId(0L); // 0表示系统发送
        message.setReceiverId(sellerId);
        message.setContent(content);
        message.setType(MessageTypeEnum.ORDER); // 订单相关通知
        message.setOrderId(orderId);
        message.setIsRead(false); // 0=未读，1=已读
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(false); // 0=未删除

        // 插入消息
        int rows = messageMapper.insert(message);
        if (rows <= 0) {
            throw new RuntimeException("通知发送失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendBuyerNotice(Long buyerId, String content, Long orderId) {
        // 校验买家是否存在
        User user = userService.getById(buyerId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS, "买家不存在");
        }

        // 创建消息实体
        Message message = new Message();
        message.setSenderId(0L); // 系统发送
        message.setReceiverId(buyerId);
        message.setContent(content);
        message.setType(MessageTypeEnum.ORDER);
        message.setOrderId(orderId);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(false);

        // 插入消息
        int rows = messageMapper.insert(message);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(Long userId, Long msgId) {
        // 校验消息是否存在且属于当前用户
        Message message = messageMapper.selectById(msgId);
        if (message == null || message.getIsDeleted()) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_EXISTS);
        }
        if (!message.getReceiverId().equals(userId) && message.getReceiverId() != -1) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 标记为已读
        int rows = messageMapper.updateReadStatus(msgId, 1); // 1=已读
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }
        return true;
    }

    @Override
    public Integer countUnreadMessages(Long userId) {
        return messageMapper.countUnread(userId);
    }

}