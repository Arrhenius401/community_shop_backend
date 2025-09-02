package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.other.SellerScoreDTO;
import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.VO.EvaluationVO;
import com.community_shop.backend.entity.Evaluation;

import java.util.List;

/**
 * 信用与评价Service接口，实现《文档》中评价提交、好评率统计、评价举报等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：评价提交、好评率统计、评价举报
 * 2. 《文档4_数据库工作（新）.docx》：user_evaluation表结构（eval_id、order_id、score等）
 * 3. 《代码文档1 Mapper层设计.docx》：EvaluationMapper的CRUD及评分统计方法
 */
public interface EvaluationService {
    // 获取所有评价
    List<Evaluation> getAllEvaluations();

    // 获取评价详情
    Evaluation getEvaluationById(int id);

    // 添加评价
    int addEvaluation(Evaluation evaluation);

    // 更新评价信息
    int updateEvaluation(Evaluation evaluation);

    // 删除评价
    int deleteEvaluation(int id);

    /**
     * 新增评价（基础CRUD）
     * 核心逻辑：记录评价时间，调用EvaluationMapper.insert插入数据
     * @param evaluation 评价实体（含orderId、userId、score、content，不含eval_id）
     * @return ResultDTO<Long> 成功返回新增评价ID，失败返回错误信息
     * @see com.community_shop.backend.mapper.EvaluationMapper#insert(Evaluation)
     */
    ResultDTO<Long> insertEvaluation(Evaluation evaluation);

    /**
     * 按评价ID查询（基础CRUD）
     * 核心逻辑：调用EvaluationMapper.selectById查询，关联UserService获取评价者信息
     * @param evalId 评价ID（主键）
     * @return ResultDTO<Evaluation> 成功返回含评价者信息的评价详情，失败返回错误信息
     * @see com.community_shop.backend.mapper.EvaluationMapper#selectById(Long)
     * @see UserService#selectUserById(Long)
     */
    ResultDTO<Evaluation> selectEvaluationById(Long evalId);

    /**
     * 按订单ID查询评价（基础CRUD）
     * 核心逻辑：调用EvaluationMapper.selectByOrderId查询，判断订单是否已评价
     * @param orderId 订单ID（与评价一对一关联）
     * @return ResultDTO<Evaluation> 成功返回评价信息（无则返回null），失败返回错误信息
     * @see com.community_shop.backend.mapper.EvaluationMapper#selectByOrderId(Long)
     */
    ResultDTO<Evaluation> selectEvaluationByOrder(Long orderId);

    /**
     * 更新评价内容（基础CRUD）
     * 核心逻辑：校验仅评价者可操作，调用EvaluationMapper.updateById更新内容
     * @param evalId 评价ID
     * @param newContent 新评价内容
     * @param userId 操作用户ID（需与评价userId一致）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息（如"无权限编辑"）
     * @see com.community_shop.backend.mapper.EvaluationMapper#updateById(Evaluation)
     */
    ResultDTO<Boolean> updateEvaluationContent(Long evalId, String newContent, Long userId);

    /**
     * 按评价ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验评价者或管理员权限，调用EvaluationMapper.deleteById标记删除
     * @param evalId 待删除评价ID
     * @param operatorId 操作用户ID（评价者或管理员）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息
     * @see com.community_shop.backend.mapper.EvaluationMapper#deleteById(Long)
     */
    ResultDTO<Boolean> deleteEvaluationById(Long evalId, Long operatorId);

    /**
     * 提交评价（业务方法）
     * 核心逻辑：校验订单状态为"已完成"、未评价，提交评价后同步更新卖家信用分（好评+5/差评-10）
     * @param evaluationVO 评价提交参数（订单ID、评分、内容）
     * @param buyerId 买家ID（评价者，需与订单buyer_id一致）
     * @return ResultDTO<String> 成功返回"评价提交成功"，失败返回错误信息
     * @see #insertEvaluation(Evaluation)
     * @see OrderService#selectOrderById(Long, Long)
     * @see UserService#updateCreditScore(Long, Integer, String)
     */
    ResultDTO<String> submitEvaluation(EvaluationVO evaluationVO, Long buyerId);

    /**
     * 计算卖家评分（业务方法）
     * 核心逻辑：调用EvaluationMapper统计平均评分、好评/中评/差评数，计算好评率
     * @param sellerId 卖家ID
     * @return ResultDTO<SellerScoreDTO> 成功返回评分详情（平均评分、好评率、各星级数量），失败返回错误信息
     * @see com.community_shop.backend.mapper.EvaluationMapper#selectAverageScore(Long)
     * @see com.community_shop.backend.mapper.EvaluationMapper#countScoreLevel(Long, Integer, Integer)
     */
    ResultDTO<SellerScoreDTO> calculateSellerScore(Long sellerId);

//    /**
//     * 举报评价（业务方法）
//     * 核心逻辑：校验评价存在性，记录举报信息，24小时内管理员审核，通过则删除评价
//     * @param evalId 被举报评价ID
//     * @param reason 举报原因（如"虚假评价"/"辱骂内容"）
//     * @param reporterId 举报者ID
//     * @return ResultDTO<String> 成功返回"举报提交成功"，失败返回错误信息
//     * @see com.community_shop.backend.mapper.EvaluationReportMapper#insert(Long, String, Long)
//     * @see #deleteEvaluationById(Long, Long)
//     */
//    ResultDTO<String> reportEvaluation(Long evalId, String reason, Long reporterId);
}
