package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Evaluation;
import com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评价模块Mapper接口，严格对应evaluation表结构（文档4_数据库设计.docx）
 */
@Mapper
public interface EvaluationMapper {



    // ==================== 基础CRUD ====================
    /**
     * 提交评价（插入评价数据）
     * @param evaluation 评价实体（含订单ID、评价者ID、评分等核心字段）
     * @return 影响行数
     */
    int insert(Evaluation evaluation);

    /**
     * 通过评价ID查询评价详情
     * @param evalId 评价唯一标识
     * @return 评价完整实体
     */
    Evaluation selectById(@Param("evalId") Long evalId);

    /**
     * 更新评价信息（评价者编辑场景）
     * @param evaluation 评价实体（含需更新的字段）
     * @return 影响行数
     */
    int updateById(Evaluation evaluation);

    /**
     * 删除评价（逻辑删除，更新status状态）
     * @param evalId 评价ID
     * @param status 目标状态（如"HIDDEN"）
     * @return 影响行数
     */
    int deleteById(@Param("evalId") Long evalId, @Param("status") EvaluationStatusEnum status);

    /**
     * 删除评价详情
     * @param evalId 评价ID
     * @return 删除结果影响行数
     */
    @Delete("DELETE FROM evaluation WHERE eval_id = #{evalId}")
    int deleteById(Long evalId);


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
     * SELECT COUNT(*) FROM evaluation
     * WHERE order_id IN (1, 2, 3)
     * AND status = 'VISIBLE';
     */
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
    @Select({"SELECT AVG(e.score) FROM evaluation e JOIN `order` o ON e.order_id = o.order_id WHERE o.product_id = #{productId}"})
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
            "SELECT COUNT(*)",
            "FROM evaluation e",
            "JOIN `order` o USING (order_id)",  // 修复：USING添加括号，order表名加反引号
            "WHERE o.product_id = #{productId}",
            "<if test='minScore != null'>AND e.score >= #{minScore}</if>",  // 优化：符号去转义
            "<if test='maxScore != null'>AND e.score <= #{maxScore}</if>",  // 优化：符号去转义
            "</script>"})
    int countProductScoreLevel(@Param("productId") Long productId,
                                @Param("minScore") Integer minScore,
                                @Param("maxScore") Integer maxScore);
}
