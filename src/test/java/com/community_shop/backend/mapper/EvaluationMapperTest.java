package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.community_shop.backend.entity.Evaluation;
import com.community_shop.backend.enums.CodeEnum.EvaluationStatusEnum;
import com.community_shop.backend.dto.evaluation.EvaluationQueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EvaluationMapper单元测试
 * 适配文档：
 * 1. 《代码文档1 Mapper层设计.docx》2.6节 EvaluationMapper接口规范
 * 2. 《代码文档0 实体类设计.docx》2.7节 Evaluation实体属性与枚举依赖
 * 3. 《中间件文档3 自定义枚举类设计.docx》枚举TypeHandler自动转换
 * 4. 《测试文档1 基础SQL脚本设计.docx》ORDER模块evaluation初始化数据
 */
@MybatisPlusTest  // 仅加载MyBatis相关Bean，实现轻量化测试
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 禁用默认数据库替换，使用H2配置
@ActiveProfiles("test")  // 启用test环境配置（加载application-test.properties）
public class EvaluationMapperTest {

    @Autowired
    private EvaluationMapper evaluationMapper;  // 注入待测试的EvaluationMapper

    // 测试复用的基础数据（从ORDER模块data初始化脚本中获取）
    private Evaluation normalEvaluation;    // 正常评价（evalId=1，orderId=3，status=NORMAL）
    private Evaluation hiddenEvaluation;     // 隐藏评价（evalId=2，orderId=4，status=HIDDEN）

    /**
     * 测试前初始化：从数据库查询基础测试评价，确保与初始化SQL数据一致
     * 适配《代码文档0》中Evaluation实体的枚举属性（status）与关联属性（orderId、userId、evaluateeId）
     */
    @BeforeEach
    void setUp() {
        // 按evalId查询（基于BaseMapper的selectById方法）
        normalEvaluation = evaluationMapper.selectById(1L);
        hiddenEvaluation = evaluationMapper.selectById(2L);

        // 断言初始化成功（确保ORDER模块evaluation相关SQL脚本已正确执行）
        assertNotNull(normalEvaluation, "初始化失败：未查询到正常评价（evalId=1）");
        assertNotNull(hiddenEvaluation, "初始化失败：未查询到隐藏评价（evalId=2）");
    }

    /**
     * 测试insert：新增评价（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - insert方法
     */
    @Test
    void insert_validEvaluation_returnsAffectedRows1() {
        // 1. 构建测试评价对象（模拟已完成订单的评价提交）
        Evaluation newEvaluation = new Evaluation();
        newEvaluation.setOrderId(5L);  // 关联已完成订单（orderId=3，参考初始化数据）
        newEvaluation.setUserId(1L);
        newEvaluation.setEvaluateeId(2L);
        newEvaluation.setContent("商品质量超预期，物流很快！");
        newEvaluation.setScore(5);     // 5星好评
        newEvaluation.setStatus(EvaluationStatusEnum.NORMAL);
        newEvaluation.setCreateTime(LocalDateTime.now());

        // 2. 执行新增方法
        int affectedRows = evaluationMapper.insert(newEvaluation);

        // 3. 断言新增结果
        assertEquals(1, affectedRows, "新增评价应影响1行数据");
        // 验证新增数据可查询
        Evaluation insertedEvaluation = evaluationMapper.selectById(newEvaluation.getEvalId());
        assertNotNull(insertedEvaluation);
        assertEquals("商品质量超预期，物流很快！", insertedEvaluation.getContent());
        assertEquals(5, insertedEvaluation.getScore());
        assertEquals(EvaluationStatusEnum.NORMAL, insertedEvaluation.getStatus());
    }

    /**
     * 测试selectByOrderId：按订单ID查询评价（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - selectByOrderId方法
     */
    @Test
    void selectByOrderId_existOrderId_returnsEvaluation() {
        // 1. 执行查询方法（查询已完成订单orderId=3的评价）
        Evaluation result = evaluationMapper.selectByOrderId(3L);

        // 2. 断言查询结果（匹配初始化的正常评价数据）
        assertNotNull(result);
        assertEquals(normalEvaluation.getEvalId(), result.getEvalId());
        assertEquals(5, result.getScore(), "订单orderId=3的评价应为5星");
        assertEquals(EvaluationStatusEnum.NORMAL, result.getStatus());
    }

    /**
     * 测试selectByOrderId：查询无评价的订单（异常场景）
     */
    @Test
    void selectByOrderId_nonEvaluationOrderId_returnsNull() {
        // 1. 执行查询方法（查询待支付订单orderId=1，该订单无评价）
        Evaluation result = evaluationMapper.selectByOrderId(5L);

        // 2. 断言查询结果（无评价订单应返回null）
        assertNull(result, "无评价的订单应返回null");
    }

    /**
     * 测试selectSellerAverageScore：统计卖家平均评分（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - selectSellerAverageScore方法
     */
    @Test
    void selectSellerAverageScore_existSellerId_returnsCorrectAverage() {
        // 1. 执行统计方法（统计test_seller（evaluateeId=2）的平均评分）
        // 初始化数据中：evalId=1（5分）、evalId=2（3分），平均分为(5+3)/2=4.0
        Double averageScore = evaluationMapper.selectSellerAverageScore(2L);

        // 2. 断言统计结果
        assertNotNull(averageScore);
        assertEquals(4.0, averageScore, 0.01, "卖家sellerId=2的平均评分应为4.0");
    }

    /**
     * 测试countSellerScoreLevel：统计卖家指定评分范围的评价数（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - countSellerScoreLevel方法
     */
    @Test
    void countSellerScoreLevel_validRange_returnsCorrectCount() {
        // 1. 执行统计方法（统计test_seller（evaluateeId=2）的好评数：4-5分）
        int goodEvalCount = evaluationMapper.countSellerScoreLevel(2L, 4, 5);

        // 2. 断言统计结果（初始化数据中仅evalId=1为4-5分，总数应为1）
        assertEquals(2, goodEvalCount, "卖家sellerId=2的4-5分评价数应为2");

        // 3. 统计中评数：3分（初始化数据中evalId=2为3分）
        int middleEvalCount = evaluationMapper.countSellerScoreLevel(2L, 3, 3);
        assertEquals(1, middleEvalCount, "卖家sellerId=2的3分评价数应为1");
    }

    /**
     * 测试selectBySellerId：分页查询卖家收到的评价（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - selectBySellerId方法
     */
    @Test
    void selectBySellerId_existSellerId_returnsEvaluationList() {
        // 1. 执行查询方法（查询test_seller（evaluateeId=2）的评价，分页：offset=0，limit=10）
        List<Evaluation> evalList = evaluationMapper.selectBySellerId(2L, 0, 10);

        // 2. 断言查询结果（初始化数据中sellerId=2有1条正常评价）
        assertNotNull(evalList);
        assertEquals(3, evalList.size(), "卖家sellerId=2应收到3条评价");

        // 验证包含正常评价和隐藏评价
        boolean hasNormalEval = evalList.stream().anyMatch(e -> e.getEvalId().equals(1L));
        boolean hasHiddenEval = evalList.stream().anyMatch(e -> e.getEvalId().equals(3L));
        assertTrue(hasNormalEval);
        assertTrue(hasHiddenEval);
    }

    /**
     * 测试selectByBuyerId：分页查询买家发布的评价（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - selectByBuyerId方法
     */
    @Test
    void selectByBuyerId_existBuyerId_returnsEvaluationList() {
        // 1. 执行查询方法（查询test_buyer（userId=1）发布的评价，分页：offset=0，limit=10）
        List<Evaluation> evalList = evaluationMapper.selectByBuyerId(1L, 0, 10);

        // 2. 断言查询结果（初始化数据中userId=1发布2条评价）
        assertNotNull(evalList);
        assertEquals(3, evalList.size(), "买家userId=1应发布3条正常评价");

        // 验证评价ID匹配
        boolean hasEval1 = evalList.stream().anyMatch(e -> e.getEvalId().equals(1L));
        boolean hasEval2 = evalList.stream().anyMatch(e -> e.getEvalId().equals(3L));   // 第二条评论是隐藏的
        assertTrue(hasEval1);
        assertTrue(hasEval2);
    }

    /**
     * 测试countByQuery：统计复杂条件下的评价总数（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - countByQuery方法
     */
    @Test
    void countByQuery_complexCondition_returnsCorrectCount() {
        // 1. 构建查询DTO（筛选：evaluateeId=2、status=NORMAL、score≥4）
        EvaluationQueryDTO queryDTO = new EvaluationQueryDTO();
        queryDTO.setEvaluateeId(2L);
        queryDTO.setScore(4);

        // 2. 执行统计方法
        int evalCount = evaluationMapper.countByQuery(queryDTO);

        // 3. 断言统计结果（初始化数据中仅evalId=1符合条件，总数应为1）
        assertEquals(1, evalCount, "符合条件的评价总数应为1");
    }

    /**
     * 测试countByOrderIds：统计多个订单的评价总数（正常场景）
     * 适配《代码文档1》2.6.2节 EvaluationMapper核心方法 - countByOrderIds方法
     */
    @Test
    void countByOrderIds_validOrderIds_returnsCorrectCount() {
        // 1. 准备订单ID列表（包含有评价的orderId=3、orderId=4，无评价的orderId=1）
        List<Long> orderIds = Arrays.asList(3L, 4L, 1L);

        // 2. 执行统计方法
        int evalCount = evaluationMapper.countByOrderIds(orderIds);

        // 3. 断言统计结果（orderId=3和orderId=4有评价，总数应为2）
        assertEquals(2, evalCount, "指定订单列表中的评价总数应为2");
    }
}