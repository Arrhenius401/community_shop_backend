package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 会话消息模块Mapper接口，对应chat_message表操作
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 批量插入会话内存
     */
    int batchInsert(@Param("list") List<ChatMessage> list);

    /**
     * 查询会话ID列表
     */
    @Select("SELECT DISTINCT chat_session_id FROM chat_message")
    List<String> selectDistinctSessionIds();

    /**
     * 根据会话ID查询所有消息（按创建时间升序）
     */
    @Select("SELECT * FROM chat_message WHERE chat_session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID删除所有记忆
     */
    @Delete("DELETE FROM chat_message WHERE chat_session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);

}