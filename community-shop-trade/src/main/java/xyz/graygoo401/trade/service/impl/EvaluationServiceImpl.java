package xyz.graygoo401.trade.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.graygoo401.api.trade.dto.evaluation.*;
import xyz.graygoo401.api.trade.enums.OrderStatusEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;
import xyz.graygoo401.api.user.util.UserUtil;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.enums.UserRoleEnum;
import xyz.graygoo401.common.exception.BusinessException;
import xyz.graygoo401.common.exception.error.SystemErrorCode;
import xyz.graygoo401.common.service.BaseServiceImpl;
import xyz.graygoo401.trade.convert.EvaluationConvert;
import xyz.graygoo401.trade.dao.entity.Evaluation;
import xyz.graygoo401.trade.dao.entity.Order;
import xyz.graygoo401.trade.dao.entity.Product;
import xyz.graygoo401.trade.dao.mapper.EvaluationMapper;
import xyz.graygoo401.trade.exception.error.EvaluationErrorCode;
import xyz.graygoo401.trade.exception.error.OrderErrorCode;
import xyz.graygoo401.trade.exception.error.ProductErrorCode;
import xyz.graygoo401.trade.service.base.EvaluationService;
import xyz.graygoo401.trade.service.base.OrderService;
import xyz.graygoo401.trade.service.base.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 评价服务实现类，处理评价相关的核心业务逻辑
 * 包括评价的增删改查、评分统计、权限校验等功能
 */
@Slf4j
@Service
public class EvaluationServiceImpl extends BaseServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    // 缓存相关常量
    private static final String CACHE_KEY_SELLER_AVG_SCORE = "evaluation:seller:avgScore:"; // 卖家平均评分缓存Key前缀
    private static final long CACHE_TTL_SELLER_AVG_SCORE = 12; // 卖家平均评分缓存有效期（小时）
    private static final int MAX_REPORT_REASON_LENGTH = 200; // 举报原因最大长度（字符）
    private static final int MIN_SCORE = 1; // 评价最低分数
    private static final int MAX_SCORE = 5; // 评价最高分数
    private static final int MAX_TAG_COUNT = 3; // 评价标签最大数量
    private static final int MAX_CONTENT_LENGTH = 500; // 评价内容最大长度（字符）
    private static final int MAX_IMAGE_COUNT = 5; // 评价图片最大数量

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EvaluationConvert evaluationConvert;


    /**
     * 提交订单评价
     *
     * @param userId        评价人ID（买家）
     * @param evalCreateDTO 评价参数
     * @return 提交成功的评价详情
     * @throws BusinessException 订单未完成、已评价、无权限等场景抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationDetailDTO submitEvaluation(Long userId, EvaluationCreateDTO evalCreateDTO) {
        try {
            // 1. 参数校验
            validateEvaluationCreateParam(evalCreateDTO);

            // 2. 校验评价人（买家）存在
            UserDTO buyer = userUtil.getUserById(userId);
            if (Objects.isNull(buyer)) {
                log.error("提交评价失败，评价人不存在，买家ID：{}", userId);
                throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
            }

            // 3. 校验订单合法性（存在、属于当前买家、状态为已完成）
            Long orderId = evalCreateDTO.getOrderId();
            Order order = orderService.getById(orderId);
            validateOrderForEvaluation(order, userId);

            // 4. 校验该订单是否已评价
            Evaluation existingEval = evaluationMapper.selectByOrderId(orderId);
            if (Objects.nonNull(existingEval)) {
                log.error("提交评价失败，订单已评价，订单ID：{}，已存在评价ID：{}", orderId, existingEval.getEvalId());
                throw new BusinessException(EvaluationErrorCode.ORDER_ALREADY_EVALUATED);
            }

            // 5. 获取关联商品与卖家信息（用于封装详情DTO）
            Product product = productService.getById(order.getProductId());
            if (Objects.isNull(product)) {
                log.error("提交评价失败，关联商品不存在，商品ID：{}", order.getProductId());
                throw new BusinessException(ProductErrorCode.PRODUCT_NOT_EXISTS);
            }
            Long sellerId = order.getSellerId();
            UserDTO seller = userUtil.getUserById(sellerId);
            if (Objects.isNull(seller)) {
                log.error("提交评价失败，关联卖家不存在，卖家ID：{}", sellerId);
                throw new BusinessException(EvaluationErrorCode.SELLER_NOT_EXISTS);
            }

            // 6. 构建Evaluation实体并插入数据库
            Evaluation evaluation = evaluationConvert.evaluationCreateDtoToEvaluation(evalCreateDTO);
            evaluation.setUserId(userId);
            int insertRows = evaluationMapper.insert(evaluation);
            if (insertRows <= 0) {
                log.error("提交评价失败，数据库插入失败，评价参数：{}", evalCreateDTO);
                throw new BusinessException(SystemErrorCode.DATA_INSERT_FAILED);
            }

            // 7. 根据评价分数调整卖家信用分（好评+5，差评-10，中评不调整）
            adjustSellerCreditScore(sellerId, evaluation.getScore());

            // 8. 清除卖家平均评分缓存（后续查询需重新统计）
            clearSellerAvgScoreCache(sellerId);

            // 9. 封装评价详情DTO并返回
            EvaluationDetailDTO detailDTO = evaluationConvert.evaluationToEvaluationDetailDTO(evaluation);
            detailDTO.setProduct(new EvaluationDetailDTO.ProductSimpleDTO(product.getProductId(), product.getTitle(), null));
            detailDTO.setEvaluator(new EvaluationDetailDTO.EvaluatorDTO(buyer.getUserId(), buyer.getUsername(), buyer.getAvatarUrl()));
            log.info("提交评价成功，评价ID：{}，订单ID：{}，买家ID：{}，卖家ID：{}",
                    evaluation.getEvalId(), orderId, userId, sellerId);
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交评价异常，买家ID：{}，评价参数：{}", userId, evalCreateDTO, e);
            throw new BusinessException(SystemErrorCode.FAILURE);
        }
    }

    /**
     * 查询订单评价
     *
     * @param orderId 订单ID
     * @return 评价详情（无评价返回null）
     */
    @Override
    public EvaluationDetailDTO getEvaluationByOrderId(Long orderId) {
        try {
            // 1. 参数校验
            if (Objects.isNull(orderId)) {
                throw new BusinessException(OrderErrorCode.ORDER_ID_NULL);
            }

            // 2. 查询评价
            Evaluation evaluation = evaluationMapper.selectByOrderId(orderId);
            if (Objects.isNull(evaluation)) {
                log.info("查询订单评价为空，订单ID：{}", orderId);
                return null;
            }

            // 3. 获取关联数据（买家、商品）
            UserDTO buyer = userUtil.getUserById(evaluation.getUserId());
            Order order = orderService.getById(orderId);
            Product product = productService.getById(order.getProductId());
            if (Objects.isNull(buyer) || Objects.isNull(product)) {
                log.error("查询订单评价失败，关联数据缺失，评价ID：{}，买家存在：{}，商品存在：{}",
                        evaluation.getEvalId(), Objects.nonNull(buyer), Objects.nonNull(product));
                throw new BusinessException(SystemErrorCode.RELATED_DATA_MISSING);
            }

            // 4. 封装详情DTO并返回
            EvaluationDetailDTO detailDTO = evaluationConvert.evaluationToEvaluationDetailDTO(evaluation);
            detailDTO.setProduct(new EvaluationDetailDTO.ProductSimpleDTO(product.getProductId(), product.getTitle(), null));
            detailDTO.setEvaluator(new EvaluationDetailDTO.EvaluatorDTO(buyer.getUserId(), buyer.getUsername(), buyer.getAvatarUrl()));
            log.info("查询订单评价成功，订单ID：{}，评价ID：{}", orderId, evaluation.getEvalId());
            return detailDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询订单评价异常，订单ID：{}", orderId, e);
            throw new BusinessException(SystemErrorCode.FAILURE);
        }
    }

    /**
     * 查询卖家的评价列表
     *
     * @param queryDTO 查询参数
     * @return 分页评价列表
     */
    @Override
    public PageResult<EvaluationListItemDTO> searchEvaluationsByQuery(EvaluationQueryDTO queryDTO) {
        try {
            // 1. 参数校验
            validateSellerAndPageParam(queryDTO);

            // 2. 校验卖家存在
            Long sellerId = queryDTO.getEvaluateeId();
            UserDTO seller = userUtil.getUserById(sellerId);
            if (Objects.isNull(seller)) {
                log.error("查询卖家评价列表失败，卖家不存在，卖家ID：{}", sellerId);
                throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
            }

            // 3. 分页查询卖家的有效评价（状态为正常）
            int pageNum = queryDTO.getPageNum() <= 0 ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() <= 0 || queryDTO.getPageSize() > 20 ? 10 : queryDTO.getPageSize(); // 限制最大页大小为20
            List<Evaluation> evaluationList = evaluationMapper.selectByQuery(queryDTO);
            long total = evaluationMapper.countByQuery(queryDTO);
            long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

            // 4. 转换为EvaluationListDTO（处理评价人脱敏、内容摘要、图片缩略图）
            List<EvaluationListItemDTO> dtoList = evaluationList.stream()
                    .map(eval -> {
                        EvaluationListItemDTO listDTO = new EvaluationListItemDTO();
                        // 评价基础信息
                        listDTO.setEvalId(eval.getEvalId());
                        listDTO.setScore(eval.getScore());
                        listDTO.setCreateTime(eval.getCreateTime());
//                        listDTO.setHelpfulCount(eval.getHelpfulCount());

                        // 评价人信息（脱敏）
                        UserDTO buyer = userUtil.getUserById(eval.getUserId());
                        if (Objects.nonNull(buyer)) {
                            EvaluationListItemDTO.EvaluatorSimpleDTO evaluatorDTO = new EvaluationListItemDTO.EvaluatorSimpleDTO();
                            // 用户名脱敏（如“张***”）
                            String username = buyer.getUsername();
                            evaluatorDTO.setUsername(desensitizeUsername(username));
                            evaluatorDTO.setAvatarUrl(buyer.getAvatarUrl());
                            listDTO.setEvaluator(evaluatorDTO);
                        }

                        // 评价内容摘要（前100字）
                        String content = eval.getContent();
                        listDTO.setContentSummary(StringUtils.hasText(content)
                                ? content.length() > 100 ? content.substring(0, 100) + "..." : content
                                : "");

//                        // 评价图片缩略图（最多3张）
//                        if (Objects.nonNull(eval.getImageUrls()) && !eval.getImageUrls().isEmpty()) {
//                            List<String> thumbImages = eval.getImageUrls().size() > 3
//                                    ? eval.getImageUrls().subList(0, 3)
//                                    : eval.getImageUrls();
//                            listDTO.setImageThumbs(thumbImages);
//                        }
//
//                        // 评价标签（最多2个）
//                        if (Objects.nonNull(eval.getTags()) && !eval.getTags().isEmpty()) {
//                            List<String> showTags = eval.getTags().size() > 2
//                                    ? eval.getTags().subList(0, 2)
//                                    : eval.getTags();
//                            listDTO.setTags(showTags);
//                        }

                        // 是否有追评（简化处理：假设存在updateTime且与createTime不同则视为有追评）
                        listDTO.setHasAdditional(Objects.nonNull(eval.getUpdateTime())
                                && !eval.getCreateTime().equals(eval.getUpdateTime()));

                        return listDTO;
                    })
                    .collect(Collectors.toList());

            // 5. 封装分页结果
            PageResult<EvaluationListItemDTO> pageResult = new PageResult<>();
            pageResult.setList(dtoList);
            pageResult.setTotal(total);
            pageResult.setTotalPages(totalPages);
            pageResult.setPageNum(pageNum);
            pageResult.setPageSize(pageSize);
            return pageResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(SystemErrorCode.FAILURE);
        }
    }

    /**
     * 更新评价内容（带权限校验）
     * @param userId 操作人ID
     * @param evaluationUpdateDTO 更新信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEvaluationContent(Long userId, EvaluationUpdateDTO evaluationUpdateDTO) {
        Long evalId = evaluationUpdateDTO.getEvalId();
        String newContent = evaluationUpdateDTO.getNewContent();

        // 1. 参数校验
        if (evalId == null || !StringUtils.hasText(newContent) || userId == null) {
            throw new BusinessException(SystemErrorCode.PARAM_NULL, "参数不能为空");
        }

        // 2. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evalId);
        if (evaluation == null) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_NOT_EXISTS);
        }

        // 3. 权限校验（仅评价者可修改）
        if (!evaluation.getUserId().equals(userId)) {
            log.warn("更新评价权限不足，评价ID：{}，操作人：{}，实际所有者：{}",
                    evalId, userId, evaluation.getUserId());
            throw new BusinessException(SystemErrorCode.PERMISSION_DENIED);
        }

        // 4. 执行更新
        evaluation.setContent(newContent);
        evaluation.setUpdateTime(LocalDateTime.now()); // 更新修改时间
        int rows = evaluationMapper.updateById(evaluation);

        // 5. 验证结果
        if (rows <= 0) {
            throw new BusinessException(SystemErrorCode.DATA_UPDATE_FAILED);
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
            throw new BusinessException(SystemErrorCode.PARAM_NULL, "参数不能为空");
        }

        // 2. 查询评价
        Evaluation evaluation = evaluationMapper.selectById(evalId);
        if (evaluation == null) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_NOT_EXISTS);
        }

        // 3. 权限校验（评价者或管理员）
        UserDTO operator = userUtil.getUserById(operatorId);
        if (operator == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_EXISTS);
        }

        boolean isOwner = evaluation.getUserId().equals(operatorId);
        boolean isAdmin = UserRoleEnum.ADMIN.equals(operator.getRole());
        if (!isOwner && !isAdmin) {
            log.warn("删除评价权限不足，评价ID：{}，操作人：{}，角色：{}",
                    evalId, operatorId, operator.getRole());
            throw new BusinessException(SystemErrorCode.PERMISSION_DENIED);
        }

        // 4. 执行删除
        int rows = evaluationMapper.deleteById(evalId);
        if (rows <= 0) {
            throw new BusinessException(SystemErrorCode.DATA_DELETE_FAILED);
        }

        // 后期可能添加的删除缓存位置

        log.info("删除评价成功，ID：{}，操作人：{}", evalId, operatorId);
        return true;
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
            throw new BusinessException(SystemErrorCode.PARAM_ERROR, "卖家ID无效");
        }

        // 2. 初始化结果对象
        SellerScoreDTO scoreDTO = new SellerScoreDTO();
        scoreDTO.setSellerId(sellerId);

        // 3. 查询平均评分
        Double avgScore = evaluationMapper.selectSellerAverageScore(sellerId);
        scoreDTO.setAverageScore(avgScore != null ? avgScore : 0.0);

        // 4. 统计各星级数量
        int fiveStar = evaluationMapper.countSellerScoreLevel(sellerId, 5, 5);
        int fourStar = evaluationMapper.countSellerScoreLevel(sellerId, 4, 4);
        int threeStar = evaluationMapper.countSellerScoreLevel(sellerId, 3, 3);
        int twoStar = evaluationMapper.countSellerScoreLevel(sellerId, 2, 2);
        int oneStar = evaluationMapper.countSellerScoreLevel(sellerId, 1, 1);

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
        scoreDTO.setTotalCount(total);

        // 未来可添加缓存模块

        log.info("计算卖家评分完成，卖家ID：{}，总评价数：{}，平均评分：{}",
                sellerId, total, scoreDTO.getAverageScore());
        return scoreDTO;
    }

    @Override
    public ProductScoreDTO calculateProductScore(Long productId) {
        // 1. 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(SystemErrorCode.PARAM_ERROR, "商品ID无效");
        }

        // 2. 初始化结果对象
        ProductScoreDTO scoreDTO = new ProductScoreDTO();
        scoreDTO.setProductId(productId);

        // 3. 查询平均评分
        Double avgScore = evaluationMapper.selectProductAverageScore(productId);
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
        scoreDTO.setTotalCount(total);

        // 未来可添加缓存模块

        log.info("计算商品评分完成，商品ID：{}，总评价数：{}，平均评分：{}",
                productId, total, scoreDTO.getAverageScore());
        return scoreDTO;
    }

    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 校验评价创建参数合法性
     */
    private void validateEvaluationCreateParam(EvaluationCreateDTO createDTO) {
        // 订单ID非空
        if (Objects.isNull(createDTO.getOrderId())) {
            throw new BusinessException(OrderErrorCode.ORDER_ID_NULL);
        }
        // 评分非空且在1-5之间
        if (Objects.isNull(createDTO.getScore()) || createDTO.getScore() < MIN_SCORE || createDTO.getScore() > MAX_SCORE) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_SCORE_INVALID);
        }
        // 评价内容非空且不超过最大长度
        if (!StringUtils.hasText(createDTO.getContent())) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_CONTENT_NULL);
        }
        if (createDTO.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_CONTENT_INVALID);
        }
        // 评价图片不超过最大数量
        if (Objects.nonNull(createDTO.getImageUrls()) && createDTO.getImageUrls().size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_IMAGE_URL_INVALID);
        }
        // 评价标签不超过最大数量
        if (Objects.nonNull(createDTO.getTags()) && createDTO.getTags().size() > MAX_TAG_COUNT) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_TAGS_INVALID);
        }
    }

    /**
     * 校验订单是否符合评价条件（存在、归属当前买家、状态已完成）
     */
    private void validateOrderForEvaluation(Order order, Long buyerId) {
        if (Objects.isNull(order)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_EXISTS);
        }
        // 订单归属校验（仅买家可评价）
        if (!order.getBuyerId().equals(buyerId)) {
            log.error("提交评价失败，订单非当前买家所有，订单ID：{}，买家ID：{}，实际订单买家ID：{}",
                    order.getOrderId(), buyerId, order.getBuyerId());
            throw new BusinessException(SystemErrorCode.PERMISSION_DENIED);
        }
        // 订单状态校验（仅“已完成”订单可评价）
        if (!OrderStatusEnum.COMPLETED.equals(order.getStatus())) {
            log.error("提交评价失败，订单状态非已完成，订单ID：{}，当前状态：{}",
                    order.getOrderId(), order.getStatus());
            throw new BusinessException(OrderErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    /**
     * 根据评价分数调整卖家信用分（好评+5，差评-10，中评不调整）
     */
    private void adjustSellerCreditScore(Long sellerId, Integer score) {
        int scoreChange = 0;
        String reason = "";
        if (score >= 4) { // 4-5星视为好评
            scoreChange = 5;
            reason = "订单评价好评（" + score + "星），信用分+5";
        } else if (score <= 2) { // 1-2星视为差评
            scoreChange = -10;
            reason = "订单评价差评（" + score + "星），信用分-10";
        } else { // 3星视为中评，不调整信用分
            log.info("订单评价为中评（3星），不调整卖家信用分，卖家ID：{}", sellerId);
            return;
        }

        // 调用用户服务调整信用分（最低0分）
        boolean adjustResult = userUtil.updateCreditScore(sellerId, scoreChange, reason);
        if (adjustResult) {
            log.info("调整卖家信用分成功，卖家ID：{}，评价分数：{}，信用分变更：{}，原因：{}",
                    sellerId, score, scoreChange, reason);
        } else {
            log.error("调整卖家信用分失败，卖家ID：{}，评价分数：{}，信用分变更：{}",
                    sellerId, score, scoreChange);
            // 信用分调整失败不回滚评价（非核心流程），仅日志告警
        }
    }

    /**
     * 校验卖家ID与分页参数合法性
     */
    private void validateSellerAndPageParam(EvaluationQueryDTO queryDTO) {
        if (Objects.isNull(queryDTO) || Objects.isNull(queryDTO.getEvaluateeId())) {
            throw new BusinessException(SystemErrorCode.PARAM_NULL);
        }
        // 分页参数默认值处理（pageNum默认1，pageSize默认10）
        if (queryDTO.getPageNum() <= 0) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        }
    }

    /**
     * 用户名脱敏（保留首字符，其余替换为*，如“张三”→“张*”，“张三丰”→“张**”）
     */
    private String desensitizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return "匿名用户";
        }
        if (username.length() == 1) {
            return username;
        }
        return username.substring(0, 1) + "*".repeat(username.length() - 1);
    }

    /**
     * 校验举报参数合法性
     */
    private void validateReportParam(Long userId, Long evalId, String reason) {
        if (Objects.isNull(userId)) {
            throw new BusinessException(SystemErrorCode.PARAM_NULL);
        }
        if (Objects.isNull(evalId)) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_ID_NULL);
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_REPORT_REASON_NULL);
        }
        if (reason.length() > MAX_REPORT_REASON_LENGTH) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_REPORT_REASON_TOO_LONG);
        }
    }

    /**
     * 清除卖家平均评分缓存
     */
    private void clearSellerAvgScoreCache(Long sellerId) {
        String cacheKey = CACHE_KEY_SELLER_AVG_SCORE + sellerId;
        Boolean deleteResult = redisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleteResult)) {
            log.info("清除卖家平均评分缓存成功，卖家ID：{}，缓存Key：{}", sellerId, cacheKey);
        } else {
            log.warn("清除卖家平均评分缓存失败（缓存不存在或已过期），卖家ID：{}，缓存Key：{}", sellerId, cacheKey);
        }
    }

}
