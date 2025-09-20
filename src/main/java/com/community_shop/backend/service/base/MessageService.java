package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.message.*;
import com.community_shop.backend.entity.Message;
import com.community_shop.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内消息服务接口，负责系统通知、用户私信等消息的发送与管理
 * 依据：
 * 1. 《文档1_需求分析.docx》：订单通知、售后通知、系统公告
 * 2. 《文档4_数据库工作（新）.docx》：message表结构（msg_id、sender_id、receiver_id等）
 */
@Service
public interface MessageService extends BaseService<Message>{

    /**
     * 发送业务触发消息（如订单支付通知、评价提醒）
     * @param userId 当前用户ID
     * @param messageSendDTO 消息参数（接收人、内容、类型、关联业务ID）
     * @return 消息ID
     * @throws BusinessException 接收人不存在、内容超限时抛出
     */
    Long sendMessage(Long userId, MessageSendDTO messageSendDTO);

    /**
     * 获取消息详情
     * @param userId 当前用户ID
     * @param msgId 消息ID
     * @return 消息详情
     * @throws BusinessException 消息不存在时抛出
     */
    MessageDetailDTO getMessageDetail(Long userId, Long msgId);

    /**
     * 标记消息状态（已读/删除）
     * @param userId 当前用户ID
     * @param statusUpdateDTO 状态更新参数（消息ID、目标状态、操作人）
     * @return 标记成功数量
     * @throws BusinessException 无权限（非接收人）时抛出
     */
    Boolean updateMessageStatus(Long userId, MessageStatusUpdateDTO statusUpdateDTO);

    /**
     * 分页查询用户消息列表
     * @param messageQueryDTO 查询参数（用户ID、类型、状态、分页）
     * @return 分页消息列表
     */
    PageResult<MessageListItemDTO> searchMessagesByQuery(Long userId, MessageQueryDTO messageQueryDTO);

    /**
     * 获取用户消息列表（不分页）
     * @param userId 用户ID
     * @param queryDTO 获取参数（用户ID、类型、状态）
     * @return 消息列表
     */
    PageResult<PrivateMessageDetailDTO> searchPrivateMessagesByQuery(Long userId, PrivateMessageQueryDTO queryDTO);

    /**
     * 获取用户最近3条未读消息预览
     * @param userId 用户ID
     * @return 未读消息预览列表
     */
    List<MessagePreviewDTO> getRecentUnreadPreviews(Long userId);

    /**
     * 发送卖家通知（订单相关）
     * 核心逻辑：创建系统通知消息，标记为"未读"，关联订单ID
     * @param sellerId 接收通知的卖家ID
     * @param content 通知内容（如"您有新订单待处理，订单号：123456"）
     * @param orderId 关联的订单ID（用于点击通知跳转订单详情）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean sendSellerNotice(Long sellerId, String content, Long orderId);

    /**
     * 发送买家通知（物流/售后相关）
     * 核心逻辑：创建系统通知消息，标记为"未读"，关联订单ID
     * @param buyerId 接收通知的买家ID
     * @param content 通知内容（如"您的订单已发货，快递单号：SF123456789"）
     * @param orderId 关联的订单ID
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean sendBuyerNotice(Long buyerId, String content, Long orderId);

    /**
     * 标记消息为已读
     * 核心逻辑：校验消息接收者为当前用户，更新消息状态为"已读"
     * @param userId 操作用户ID（需与消息receiver_id一致）
     * @param msgId 消息ID
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean markAsRead(Long userId, Long msgId);

    /**
     * 统计用户未读消息数量
     * 核心逻辑：按接收者ID和状态为"未读"查询计数
     * @param userId 用户ID
     * @return 未读消息数
     */
    Integer countUnreadMessages(Long userId);

}
