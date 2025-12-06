package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.dto.evaluation.EvaluationQueryDTO;
import com.community_shop.backend.entity.Evaluation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评价模块Mapper接口，严格对应evaluation表结构（文档4_数据库设计.docx）
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    // ==================== 关联查询 ====================
    /**
     * 通过订单ID查询评价（判断是否已评价）
     * @param orderId 订单ID
     * @return 评价实体（null表示未评价）
     */
    Evaluation selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 分页查询卖家收到的评价
     * @param sellerId 被评价者（卖家）ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 评价分页列表
     */
    List<Evaluation> selectBySellerId(
            @Param("sellerId") Long sellerId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 分页查询买家发布的评价
     * @param buyerId 评价者（买家）ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 评价分页列表
     */
    List<Evaluation> selectByBuyerId(
            @Param("buyerId") Long buyerId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 分页查询评价列表（高级搜索）
     * @param queryDTO 查询参数
     * @return 评价分页列表
     */
    List<Evaluation> selectByQuery(@Param("query") EvaluationQueryDTO queryDTO);

    /**
     * 统计评价列表（高级搜索）
     * @param queryDTO 搜索参数
     * @return 评价数量
     */
    Integer countByQuery(@Param("query") EvaluationQueryDTO queryDTO);

    // ==================== 统计分析 ====================
    /**
     * 统计卖家的平均评分
     * @param sellerId 卖家ID
     * @return 平均评分（保留1位小数）
     */
    Double selectSellerAverageScore(@Param("sellerId") Long sellerId);

    /**
     * 统计卖家指定评分范围内的评价数量
     * @param sellerId 卖家ID
     * @param minScore 最低评分
     * @param maxScore 最高评分
     * @return 评价数量
     */
    int countSellerScoreLevel(
            @Param("sellerId") Long sellerId,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore
    );

    /**
     * 统计多个订单的评价总数（批量判断是否已评价）
     * @param orderIds 订单ID列表
     * @return 已评价数量
     */
    int countByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 统计指定产品的平均评分（好评率计算）
     * @param productId 产品ID
     * @return 平均评分
     */
    Double selectProductAverageScore(Long productId);

    /**
     * 统计指定产品的评分数量（好评/中评/差评数）
     * @param productId 产品ID
     * @param minScore 最低评分（含）
     * @param maxScore 最高评分（含）
     * @return 评分范围内的评价数量
     */
    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM evaluation e",
            "JOIN `order` o USING (order_id)",
            "WHERE o.product_id = #{productId}",
            "<if test='minScore != null'>AND e.score &gt;= #{minScore}</if>",   // 是否需要转义取决于 SQL 语句是否被 MyBatis 当作 XML 格式解析。
            "<if test='maxScore != null'>AND e.score &lt;= #{maxScore}</if>",
            "</script>"})
    int countProductScoreLevel(@Param("productId") Long productId,
                                @Param("minScore") Integer minScore,
                                @Param("maxScore") Integer maxScore);
}
