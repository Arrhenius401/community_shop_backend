package xyz.graygoo401.ai.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xyz.graygoo401.ai.dao.entity.ChatSession;
import xyz.graygoo401.api.ai.dto.session.ChatSessionQueryDTO;

import java.util.List;

/**
 * AI 会话模块Mapper接口，对应chat_session表操作
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 更新会话标题
     * @param sessionId 会话ID
     * @param title 标题
     * @return 影响行数
     */
    @Update("UPDATE chat_session SET title = #{title} WHERE chat_session_id = #{sessionId}")
    int updateTitle(String sessionId, String title);

    /**
     * 查询会话ID列表
     * @return 会话ID列表
     */
    @Select("SELECT DISTINCT chat_session_id FROM chat_session ORDER BY update_time DESC LIMIT 0, 20")
    List<String> selectDistinctSessionIds();

    /**
     * 根据筛选参数查询会话数量
     * @param queryDTO 筛选参数
     * @return 会话数量
     */
    int countByQuery(ChatSessionQueryDTO queryDTO);

    /**
     * 根据筛选参数查询会话列表
     * @param queryDTO 筛选参数
     * @return 会话列表
     */
    List<ChatSession> selectByQuery(ChatSessionQueryDTO queryDTO);
}