package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.param.PageParam;
import com.community_shop.backend.DTO.result.PageResult;
import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.entity.Message;

/**
 * 站内消息服务接口，负责系统通知、用户私信等消息的发送与管理
 * 依据：
 * 1. 《文档1_需求分析.docx》：订单通知、售后通知、系统公告
 * 2. 《文档4_数据库工作（新）.docx》：message表结构（msg_id、sender_id、receiver_id等）
 */
public interface MessageService {
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
     * 发送系统公告（管理员向所有用户）
     * 核心逻辑：创建系统公告消息，所有用户可见，标记为"系统消息"类型
     * @param content 公告内容（如"平台将于2023年10月1日进行系统维护"）
     * @param operatorId 操作管理员ID（需校验管理员权限）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean sendSystemAnnouncement(String content, Long operatorId);

    /**
     * 查询用户的消息列表（分页）
     * 核心逻辑：按接收者ID查询，支持按消息类型筛选（系统通知/订单通知），按创建时间倒序
     * @param userId 接收消息的用户ID
     * @param msgType 消息类型（"SYSTEM"=系统消息，"ORDER"=订单消息，null=全部）
     * @param pageParam 分页参数（页码、每页条数）
     * @return 分页消息列表
     */
    PageResult<Message> selectUserMessages(Long userId, String msgType, PageParam pageParam);

    /**
     * 标记消息为已读
     * 核心逻辑：校验消息接收者为当前用户，更新消息状态为"已读"
     * @param msgId 消息ID
     * @param userId 操作用户ID（需与消息receiver_id一致）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean markAsRead(Long msgId, Long userId);

    /**
     * 统计用户未读消息数量
     * 核心逻辑：按接收者ID和状态为"未读"查询计数
     * @param userId 用户ID
     * @return 未读消息数
     */
    Integer countUnreadMessages(Long userId);

    /**
     * 删除消息（逻辑删除）
     * 核心逻辑：校验消息接收者为当前用户，标记消息为"已删除"
     * @param msgId 消息ID
     * @param userId 操作用户ID（需与消息receiver_id一致）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean deleteMessage(Long msgId, Long userId);
}
