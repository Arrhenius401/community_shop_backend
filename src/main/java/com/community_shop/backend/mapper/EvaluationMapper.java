package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Evaluation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationMapper {

    // 基础CRUD

    /**
     * 提交评价（交易完成后）
     * @param evaluation 评价实体
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO evaluation(order_id, buyer_id, seller_id, score, content, create_time) " +
            "VALUES(#{orderId}, #{buyerId}, #{sellerId}, #{score}, #{content}, #{createTime})")
    int insert(Evaluation evaluation);

    /**
     * 查询评价详情
     * @param evalId 评价ID
     * @return 评价实体
     */
    @Select("SELECT * FROM evaluation WHERE eval_id = #{evalId}")
    Evaluation selectById(Long evalId);

    /**
     * 更新评价信息
     * @param evaluation 评价实体
     * @return 更新结果影响行数
     */
    @Update("UPDATE evaluation SET order_id = #{orderId}, buyer_id = #{buyerId}, seller_id = #{sellerId}, " +
            "score = #{score}, content = #{content}, create_time = #{createTime} WHERE eval_id = #{evalId}")
    int updateById(Evaluation evaluation);

    /**
     * 删除评价详情
     * @param evalId 评价ID
     * @return 删除结果影响行数
     */
    @Delete("DELETE FROM evaluation WHERE eval_id = #{evalId}")
    int deleteById(Long evalId);

    // 关联查询

    /**
     * 查询订单对应的评价（判断是否已评价）
     * @param evalId 订单ID
     * @return 评价实体
     */
    @Select("SELECT * FROM evaluation WHERE eval_id = #{evalId}")
    Evaluation selectByOrderId(Long evalId);

    /**
     * 查询卖家收到的评价（卖家信用展示）
     * @param sellerId 卖家ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评价列表
     */
    @Select("SELECT * FROM evaluation WHERE seller_id = #{sellerId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Evaluation> selectBySellerId(@Param("sellerId") Long sellerId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    /**
     * 统计卖家的平均评分（好评率计算）
     * @param sellerId 卖家ID
     * @return 平均评分
     */
    @Select("SELECT AVG(score) FROM evaluation WHERE seller_id = #{sellerId}")
    Double selectSellerAverageScore(Long sellerId);

    /**
     * 统计指定卖家在指定评分范围内的评价数量（好评/中评/差评数）
     * @param sellerId 卖家ID
     * @param minScore 最低评分（含）
     * @param maxScore 最高评分（含）
     * @return 评分范围内的评价数量
     */
    // <	小于  	&lt;	避免被解析为 XML 标签的开始（greater than）
    // >	大于	    &gt;	避免被解析为 XML 标签的结束（less than）
    // &	逻辑与	&amp;	XML 中 & 是实体引用的起始符号，必须转义（ampersand，连字符 / 与符号）
    // '	单引号	&apos;	在属性值或字符串中使用时可能需要转义（apostrophe,撇号 / 单引号）
    // "	双引号	&quot;	当属性值用双引号包裹时，内部双引号需转义
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM evaluation WHERE seller_id = #{sellerId}",
            "<if test='minScore != null'>AND score &gt;= #{minScore}</if>",
            "<if test='maxScore != null'>AND score &lt;= #{maxScore}</if>",
            "</script>"
    })
    int countSellerScoreLevel(@Param("sellerId") Long sellerId,
                        @Param("minScore") Integer minScore,
                        @Param("maxScore") Integer maxScore);


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
    // order 是 SQL 关键字（用于排序的 ORDER BY），作为表名时必须用反引号 ` 包裹，否则数据库会将其解析为关键字而非表名，导致逻辑错误。
    // 在 MyBatis 的 @Select 注解中，>=、<= 等比较符号可以直接书写，无需转义为 &gt;=、&lt;=（转义通常用于 XML 配置文件，注解中冗余且降低可读性）。
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
