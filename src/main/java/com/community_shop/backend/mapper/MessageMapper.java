package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * 消息Mapper接口，提供message表的CRUD操作
 */
public interface MessageMapper {
    /**
     * 新增消息
     * @param message 消息实体
     * @return 插入影响行数
     */
    int insert(Message message);

    /**
     * 按ID查询消息
     * @param msgId 消息ID
     * @return 消息实体
     */
    Message selectById(Long msgId);

    /**
     * 按接收者查询消息（分页）
     * @param receiverId 接收者ID
     * @param msgType 消息类型（可为null）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 消息列表
     */
    List<Message> selectByReceiver(
            @Param("receiverId") Long receiverId,
            @Param("msgType") String msgType,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计接收者的消息总数
     * @param receiverId 接收者ID
     * @param msgType 消息类型（可为null）
     * @return 总条数
     */
    Integer countByReceiver(
            @Param("receiverId") Long receiverId,
            @Param("msgType") String msgType
    );

    /**
     * 更新消息阅读状态
     * @param msgId 消息ID
     * @param isRead 阅读状态（0=未读，1=已读）
     * @return 更新影响行数
     */
    int updateReadStatus(@Param("msgId") Long msgId, @Param("isRead") int isRead);

    /**
     * 统计未读消息数
     * @param receiverId 接收者ID
     * @return 未读消息数
     */
    int countUnread(@Param("receiverId") Long receiverId);

    /**
     * 更新消息删除状态（逻辑删除）
     * @param msgId 消息ID
     * @param isDeleted 删除状态（0=未删除，1=已删除）
     * @return 更新影响行数
     */
    int updateDeleteStatus(@Param("msgId") Long msgId, @Param("isDeleted") int isDeleted);
}
