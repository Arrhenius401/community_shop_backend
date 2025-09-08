package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.evaluation.ProductScoreDTO;
import com.community_shop.backend.dto.evaluation.SellerScoreDTO;
import com.community_shop.backend.dto.evaluation.EvaluationCreateDTO;
import com.community_shop.backend.dto.evaluation.EvaluationUpdateDTO;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Evaluation;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.EvaluationMapper;
import com.community_shop.backend.service.base.EvaluationService;
import com.community_shop.backend.service.base.OrderService;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 评价服务实现类，处理评价相关的核心业务逻辑
 * 包括评价的增删改查、评分统计、权限校验等功能
 */
@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {
    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;


    /**
     * 插入评价并返回ID（事务控制）
     * @param evaluation 评价实体
     * @return 评价ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertEvaluation(Evaluation evaluation) {
        // 1. 参数校验
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "评价信息不能为空");
        }

        // 2. 设置时间戳
        evaluation.setCreateTime(LocalDateTime.now());

        // 3. 执行插入
        evaluationMapper.insert(evaluation);

        // 4. 验证插入结果
        if (evaluation.getEvalId() == null) {
            log.error("插入评价后未获取到ID，数据：{}", evaluation);
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
        }

        log.info("插入评价成功，ID：{}", evaluation.getEvalId());
        return evaluation.getEvalId();
    }

    /**
     * 按ID查询评价（带用户信息）
     * @param evalId 评价ID
     * @return 评价实体（含评价者信息）
     */
    @Override
    public Evaluation selectEvaluationById(Long evalId) {
        // 1. 参数校验
        if (evalId == null || evalId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价ID无效");
        }

        // 2. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evalId);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_EXISTS);
        }

//        // 3. 补充评价者信息
//        if (evaluation.getUserId() != null) {
//            User evaluator = userService.selectUserById(evaluation.getUserId());
//            if (evaluator != null) {
//                // 可根据需要设置用户名等非敏感信息
//                evaluation.setEvaluatorName(evaluator.getNickname());
//            }
//        }

        log.info("查询评价详情成功，ID：{}", evalId);
        return evaluation;
    }

    /**
     * 按订单查询评价
     * @param orderId 订单ID
     * @return 评价实体
     */
    @Override
    public Evaluation selectEvaluationByOrder(Long orderId) {
        // 1. 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID无效");
        }

        // 2. 执行查询
        Evaluation evaluation = evaluationMapper.selectByOrderId(orderId);
        log.info("查询订单评价，订单ID：{}，评价存在：{}", orderId, evaluation != null);

        return evaluation;
    }

    /**
     * 更新评价内容（带权限校验）
     * @param evaluationUpdateDTO 更新信息
     * @param userId 操作人ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEvaluationContent(EvaluationUpdateDTO evaluationUpdateDTO, Long userId) {
        Long evalId = evaluationUpdateDTO.getEvalId();
        String newContent = evaluationUpdateDTO.getNewContent();

        // 1. 参数校验
        if (evalId == null || !StringUtils.hasText(newContent) || userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "参数不能为空");
        }

        // 2. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evalId);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_EXISTS);
        }

        // 3. 权限校验（仅评价者可修改）
        if (!evaluation.getUserId().equals(userId)) {
            log.warn("更新评价权限不足，评价ID：{}，操作人：{}，实际所有者：{}",
                    evalId, userId, evaluation.getUserId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 4. 执行更新
        evaluation.setContent(newContent);
        evaluation.setUpdateTime(LocalDateTime.now()); // 更新修改时间
        int rows = evaluationMapper.updateById(evaluation);

        // 5. 验证结果
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
        }

        log.info("更新评价内容成功，ID：{}", evalId);
        return true;
    }

    /**
     * 删除评价（带权限校验）
     * @param evalId 评价ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteEvaluationById(Long evalId, Long operatorId) {
        // 1. 参数校验
        if (evalId == null || operatorId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "参数不能为空");
        }

        // 2. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evalId);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_EXISTS);
        }

        // 3. 权限校验（评价者或管理员）
        User operator = userService.selectUserById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        boolean isOwner = evaluation.getUserId().equals(operatorId);
        boolean isAdmin = UserRoleEnum.ADMIN.equals(operator.getRole());
        if (!isOwner && !isAdmin) {
            log.warn("删除评价权限不足，评价ID：{}，操作人：{}，角色：{}",
                    evalId, operatorId, operator.getRole());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 4. 执行删除
        int rows = evaluationMapper.deleteById(evalId);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
        }

        log.info("删除评价成功，ID：{}，操作人：{}", evalId, operatorId);
        return true;
    }

    /**
     * 提交评价（核心业务方法）
     * @param evaluationCreateDTO 评价VO
     * @param buyerId 买家ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitEvaluation(EvaluationCreateDTO evaluationCreateDTO, Long buyerId) {
        // 1. 参数校验
        if (evaluationCreateDTO == null || evaluationCreateDTO.getOrderId() == null ||
                evaluationCreateDTO.getScore() == null || buyerId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "评价参数不完整");
        }

        // 2. 验证评分范围（1-5星）
        if (evaluationCreateDTO.getScore() < 1 || evaluationCreateDTO.getScore() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评分必须在1-5之间");
        }

        // 3. 校验订单状态
        Long orderId = evaluationCreateDTO.getOrderId();
        Order order = orderService.selectOrderById(orderId, buyerId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS, "订单不存在或无权评价");
        }

        // 4. 验证订单是否已完成
        if (!"已完成".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "仅已完成订单可评价");
        }

        // 5. 校验是否已评价
        Evaluation existingEval = evaluationMapper.selectByOrderId(orderId);
        if (existingEval != null) {
            throw new BusinessException(ErrorCode.OPERATION_REPEAT, "该订单已评价，不可重复评价");
        }

        // 6. 构建评价实体
        Evaluation evaluation = new Evaluation(evaluationCreateDTO);
        evaluation.setSellerId(order.getSellerId());

        // 7. 保存评价
        evaluationMapper.insert(evaluation);
        if (evaluation.getEvalId() == null) {
            throw new BusinessException(ErrorCode.DATA_INSERT_FAILED, "评价提交失败");
        }

        // 8. 同步更新卖家信用分
        // 规则：好评(4-5星)+5分，差评(1-2星)-3分，中评(3星)不调整
        int creditChange = evaluation.getScore() >= 4 ? 5 :
                (evaluation.getScore() <= 2 ? -3 : 0);

        if (creditChange != 0) {
            userService.updateCreditScore(order.getSellerId(), creditChange, "评价信用分调整");
            log.info("卖家信用分调整，卖家ID：{}，调整分数：{}，评价ID：{}",
                    order.getSellerId(), creditChange, evaluation.getEvalId());
        }

        log.info("评价提交成功，评价ID：{}，订单ID：{}", evaluation.getEvalId(), orderId);
        return "评价提交成功";
    }

    /**
     * 计算卖家评分统计
     * @param sellerId 卖家ID
     * @return 评分统计DTO
     */
    @Override
    public SellerScoreDTO calculateSellerScore(Long sellerId) {
        // 1. 参数校验
        if (sellerId == null || sellerId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "卖家ID无效");
        }

        // 2. 初始化结果对象
        SellerScoreDTO scoreDTO = new SellerScoreDTO();
        scoreDTO.setSellerId(sellerId);

        // 3. 查询平均评分
        Double avgScore = evaluationMapper.selectSellerAverageScore(sellerId);
        scoreDTO.setAverageScore(avgScore != null ? avgScore : 0.0);

        // 4. 统计各星级数量
        int fiveStar = evaluationMapper.countProductScoreLevel(sellerId, 5, 5);
        int fourStar = evaluationMapper.countProductScoreLevel(sellerId, 4, 4);
        int threeStar = evaluationMapper.countProductScoreLevel(sellerId, 3, 3);
        int twoStar = evaluationMapper.countProductScoreLevel(sellerId, 2, 2);
        int oneStar = evaluationMapper.countProductScoreLevel(sellerId, 1, 1);

        // 5. 设置星级统计数据
        scoreDTO.setFiveStarCount(fiveStar);
        scoreDTO.setFourStarCount(fourStar);
        scoreDTO.setThreeStarCount(threeStar);
        scoreDTO.setTwoStarCount(twoStar);
        scoreDTO.setOneStarCount(oneStar);

        // 6. 计算好评率（4-5星视为好评）
        int total = fiveStar + fourStar + threeStar + twoStar + oneStar;
        int positiveCount = fiveStar + fourStar;
        int negativeCount = twoStar + oneStar;
        scoreDTO.setPositiveRate(total > 0 ? (double) positiveCount / total * 100 : 0.0);
        scoreDTO.setNegativeRate(total > 0 ? (double) negativeCount / total * 100 : 0.0);

        log.info("计算卖家评分完成，卖家ID：{}，总评价数：{}，平均评分：{}",
                sellerId, total, scoreDTO.getAverageScore());
        return scoreDTO;
    }

    @Override
    public ProductScoreDTO calculateProductScore(Long productId) {
        // 1. 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID无效");
        }

        // 2. 初始化结果对象
        ProductScoreDTO scoreDTO = new ProductScoreDTO();
        scoreDTO.setProductId(productId);

        // 3. 查询平均评分
        Double avgScore = evaluationMapper.selectSellerAverageScore(productId);
        scoreDTO.setAverageScore(avgScore != null ? avgScore : 0.0);

        // 4. 统计各星级数量
        int fiveStar = evaluationMapper.countProductScoreLevel(productId, 5, 5);
        int fourStar = evaluationMapper.countProductScoreLevel(productId, 4, 4);
        int threeStar = evaluationMapper.countProductScoreLevel(productId, 3, 3);
        int twoStar = evaluationMapper.countProductScoreLevel(productId, 2, 2);
        int oneStar = evaluationMapper.countProductScoreLevel(productId, 1, 1);

        // 5. 设置星级统计数据
        scoreDTO.setFiveStarCount(fiveStar);
        scoreDTO.setFourStarCount(fourStar);
        scoreDTO.setThreeStarCount(threeStar);
        scoreDTO.setTwoStarCount(twoStar);
        scoreDTO.setOneStarCount(oneStar);

        // 6. 计算好评率（4-5星视为好评）
        int total = fiveStar + fourStar + threeStar + twoStar + oneStar;
        int positiveCount = fiveStar + fourStar;
        int negativeCount = twoStar + oneStar;
        scoreDTO.setPositiveRate(total > 0 ? (double) positiveCount / total * 100 : 0.0);
        scoreDTO.setNegativeRate(total > 0 ? (double) negativeCount / total * 100 : 0.0);

        log.info("计算商品评分完成，商品ID：{}，总评价数：{}，平均评分：{}",
                productId, total, scoreDTO.getAverageScore());
        return scoreDTO;
    }

//    /**
//     * 举报评价功能实现
//     * @param evalId 评价ID
//     * @param reason 举报原因
//     * @param reporterId 举报人ID
//     * @return 操作结果
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public String reportEvaluation(Long evalId, String reason, Long reporterId) {
//        // 1. 参数校验
//        if (evalId == null || !StringUtils.hasText(reason) || reporterId == null) {
//            throw new BusinessException(ErrorCode.PARAM_NULL, "举报参数不完整");
//        }
//
//        // 2. 验证评价存在性
//        Evaluation evaluation = evaluationMapper.selectById(evalId);
//        if (evaluation == null) {
//            throw new BusinessException(ErrorCode.EVALUATION_NOT_EXISTS);
//        }
//
//        // 3. 验证举报人身份
//        User reporter = userService.selectUserById(reporterId);
//        if (reporter == null) {
//            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
//        }
//
//        // 4. 记录举报信息（实际项目中应有举报记录表）
//        // evaluationMapper.insertReport(evalId, reporterId, reason, new Date());
//
//        log.info("评价举报成功，评价ID：{}，举报人：{}，原因：{}", evalId, reporterId, reason);
//        return "举报提交成功，平台将尽快处理";
//    }
}
