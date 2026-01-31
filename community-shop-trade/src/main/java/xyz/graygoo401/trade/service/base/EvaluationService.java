package xyz.graygoo401.trade.service.base;

import org.springframework.stereotype.Service;
import xyz.graygoo401.api.trade.dto.evaluation.*;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.exception.BusinessException;

/**
 * 信用与评价Service接口，实现《文档》中评价提交、好评率统计、评价举报等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：评价提交、好评率统计、评价举报
 * 2. 《文档4_数据库工作（新）.docx》：user_evaluation表结构（eval_id、order_id、score等）
 * 3. 《代码文档1 Mapper层设计.docx》：EvaluationMapper的CRUD及评分统计方法
 */
@Service
public interface EvaluationService {

    /**
     * 提交订单评价
     *
     * @param userId        评价人ID（买家）
     * @param evalCreateDTO 评价参数
     * @return 提交成功的评价详情
     * @throws BusinessException 订单未完成、已评价、无权限等场景抛出
     */
    EvaluationDetailDTO submitEvaluation(Long userId, EvaluationCreateDTO evalCreateDTO);

    /**
     * 查询订单评价
     *
     * @param orderId 订单ID
     * @return 评价详情（无评价返回null）
     */
    EvaluationDetailDTO getEvaluationByOrderId(Long orderId);

    /**
     * 查询卖家的评价列表
     *
     * @param queryDTO 查询参数
     * @return 分页评价列表
     */
    PageResult<EvaluationListItemDTO> searchEvaluationsByQuery(EvaluationQueryDTO queryDTO);

    /**
     * 更新评价内容（基础CRUD）
     * 核心逻辑：校验仅评价者可操作，调用EvaluationMapper.updateById更新内容
     *
     * @param userId              操作用户ID（需与评价userId一致）
     * @param evaluationUpdateDTO 评价更新参数（评价ID、新内容）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean updateEvaluationContent(Long userId, EvaluationUpdateDTO evaluationUpdateDTO);

    /**
     * 按评价ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验评价者或管理员权限，调用EvaluationMapper.deleteById标记删除
     *
     * @param evalId     待删除评价ID
     * @param operatorId 操作用户ID（评价者或管理员）
     * @return 成功返回true，失败抛出异常或返回false
     */
    Boolean deleteEvaluationById(Long evalId, Long operatorId);

    /**
     * 计算卖家评分（业务方法）
     * 核心逻辑：调用EvaluationMapper统计平均评分、好评/中评/差评数，计算好评率
     *
     * @param sellerId 卖家ID
     * @return 评分详情（平均评分、好评率、各星级数量）
     */
    SellerScoreDTO calculateSellerScore(Long sellerId);

    /**
     * 统计商品评分（业务方法）
     * 核心逻辑：调用EvaluationMapper统计商品评分
     *
     * @param productId 商品ID
     */
    ProductScoreDTO calculateProductScore(Long productId);

}
