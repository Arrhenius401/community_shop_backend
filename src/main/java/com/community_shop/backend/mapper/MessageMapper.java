package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Message;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * 消息Mapper接口，提供message表的CRUD操作
 */
public interface MessageMapper {


    // ==================== 基础CRUD ====================
    /**
     * 新增消息（发送通知）
     * @param message 消息实体（含接收人ID、类型、内容等核心字段）
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * 通过消息ID查询消息详情
     * @param msgId 消息唯一标识
     * @return 消息完整实体
     */
    Message selectById(@Param("msgId") Long msgId);


    // ==================== 状态更新 ====================
    /**
     * 更新消息阅读状态
     * @param msgId 消息ID
     * @param isRead 阅读状态（0=未读，1=已读）
     * @return 更新影响行数
     */
    int updateReadStatus(@Param("msgId") Long msgId, @Param("isRead") int isRead);

    /**
     * 更新消息删除状态（逻辑删除）
     * @param msgId 消息ID
     * @param isDeleted 删除状态（0=未删除，1=已删除）
     * @return 更新影响行数
     */
    int updateDeleteStatus(@Param("msgId") Long msgId, @Param("isDeleted") int isDeleted);

    /**
     * 批量更新消息阅读状态
     * @param msgIds 消息ID列表
     * @param isRead 阅读状态（0-未读，1-已读）
     * @return 影响行数
     */
    int batchUpdateReadStatus(@Param("msgIds") List<Long> msgIds, @Param("isRead") int isRead);

    /**
     * 批量更新用户消息删除状态
     * @param receiverId 接收人ID
     * @param isDeleted 删除状态（0-未删，1-已删）
     * @return 影响行数
     */
    int batchUpdateDeleteStatus(@Param("receiverId") Long receiverId, @Param("isDeleted") int isDeleted);


    // ==================== 查询与统计 ====================
    /**
     * 按接收人+消息类型分页查询消息
     * @param receiverId 接收人ID（-1表示全员通知）
     * @param msgType 消息类型（枚举，可为null）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 消息分页列表
     */
    List<Message> selectByReceiver(
            @Param("receiverId") Long receiverId,
            @Param("msgType") MessageTypeEnum msgType,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计接收人指定类型的消息总数
     * @param receiverId 接收人ID
     * @param msgType 消息类型（枚举，可为null）
     * @return 消息总数
     */
    int countByReceiver(@Param("receiverId") Long receiverId, @Param("msgType") MessageTypeEnum msgType);

    /**
     * 统计接收人的未读消息总数
     * @param receiverId 接收人ID
     * @return 未读消息数
     */
    int countUnread(@Param("receiverId") Long receiverId);

    /**
     * 按类型统计接收人的未读消息数
     * @param receiverId 接收人ID
     * @param msgType 消息类型（枚举）
     * @return 按类型统计的未读消息数
     */
    int countUnreadByType(@Param("receiverId") Long receiverId, @Param("msgType") MessageTypeEnum msgType);
}
