package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.evaluation.*;
import com.community_shop.backend.service.base.EvaluationService;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评价管理模块Controller，负责评价提交、编辑、查询及评分统计等接口实现
 */
@RestController
@RequestMapping("/api/v1/evaluations")
@Tag(name = "评价管理接口", description = "包含评价提交、编辑、删除、查询及卖家/商品评分统计等功能")
@Validated
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 提交订单评价接口
     * @param evaluationCreateDTO 评价创建请求参数（订单ID、评分、评价内容等）
     * @return 包含评价详情的统一响应
     */
    @PostMapping("/submit")
    @LoginRequired
    @Operation(
            summary = "提交订单评价接口",
            description = "买家针对已完成订单提交评价，会联动调整卖家信用分，登录后访问"
    )
    public ResultVO<EvaluationDetailDTO> submitEvaluation(
            @Valid @RequestBody EvaluationCreateDTO evaluationCreateDTO) {
        Long currentUserId = parseUserIdFromToken();
        EvaluationDetailDTO evaluationDetail = evaluationService.submitEvaluation(currentUserId, evaluationCreateDTO);
        return ResultVO.success(evaluationDetail);
    }

    /**
     * 编辑评价内容接口
     * @param evaluationUpdateDTO 评价更新请求参数（评价ID、新内容、新评分等）
     * @return 包含更新后评价详情的统一响应
     */
    @PutMapping("/update")
    @LoginRequired
    @Operation(
            summary = "编辑评价内容接口",
            description = "评价者修改自身提交的评价内容，会重新计算卖家信用分，登录后访问"
    )
    public ResultVO<Boolean> updateEvaluationContent(
            @Valid @RequestBody EvaluationUpdateDTO evaluationUpdateDTO) {
        Long currentUserId = parseUserIdFromToken();
        Boolean updatedEvaluation = evaluationService.updateEvaluationContent(currentUserId, evaluationUpdateDTO);
        return ResultVO.success(updatedEvaluation);
    }

    /**
     * 删除评价接口
     * @param evalId 目标评价ID
     * @return 删除结果的统一响应
     */
    @DeleteMapping("/{evalId}")
    @LoginRequired
    @Operation(
            summary = "删除评价接口",
            description = "评价者或管理员删除评价，会重新计算卖家信用分，登录后访问"
    )
    public ResultVO<Boolean> deleteEvaluationById(@PathVariable Long evalId) {
        Long currentOperatorId = parseUserIdFromToken();
        Boolean deleteResult = evaluationService.deleteEvaluationById(evalId, currentOperatorId);
        return ResultVO.success(deleteResult);
    }

    /**
     * 获取订单评价详情接口
     * @param orderId 关联订单ID
     * @return 包含评价详情的统一响应
     */
    @GetMapping("/order/{orderId}")
    @LoginRequired
    @Operation(
            summary = "获取订单评价详情接口",
            description = "查询指定订单的评价信息，需校验订单关联权限，登录后访问"
    )
    public ResultVO<EvaluationDetailDTO> getEvaluationByOrderId(@PathVariable Long orderId) {
        Long currentUserId = parseUserIdFromToken();
        EvaluationDetailDTO evaluationDetail = evaluationService.getEvaluationByOrderId(orderId);
        return ResultVO.success(evaluationDetail);
    }

    /**
     * 查询商品评价列表接口
     * @param productId 目标商品ID
     * @param evaluationQueryDTO 评价查询参数（评分范围、分页信息等）
     * @return 包含分页评价列表的统一响应
     */
    @GetMapping("/product/{productId}")
    @Operation(
            summary = "查询商品评价列表接口",
            description = "查询指定商品的所有评价，支持评分筛选与分页，无需登录"
    )
    public ResultVO<PageResult<EvaluationListItemDTO>> searchProductEvaluations(
            @PathVariable Long productId,
            @Valid @ModelAttribute EvaluationQueryDTO evaluationQueryDTO) {
        evaluationQueryDTO.setProductId(productId);
        PageResult<EvaluationListItemDTO> evaluationList = evaluationService.searchEvaluationsByQuery(evaluationQueryDTO);
        return ResultVO.success(evaluationList);
    }

    /**
     * 统计卖家评分接口
     * @param sellerId 目标卖家ID
     * @return 包含卖家评分统计数据的统一响应
     */
    @GetMapping("/seller/{sellerId}/score")
    @Operation(
            summary = "统计卖家评分接口",
            description = "统计指定卖家的平均分、好评率及各星级数量，无需登录"
    )
    public ResultVO<SellerScoreDTO> calculateSellerScore(@PathVariable Long sellerId) {
        SellerScoreDTO sellerScore = evaluationService.calculateSellerScore(sellerId);
        return ResultVO.success(sellerScore);
    }

    /**
     * 统计商品评分接口
     * @param productId 目标商品ID
     * @return 包含商品评分统计数据的统一响应
     */
    @GetMapping("/product/{productId}/score")
    @Operation(
            summary = "统计商品评分接口",
            description = "统计指定商品的平均分及各星级数量，无需登录"
    )
    public ResultVO<ProductScoreDTO> calculateProductScore(@PathVariable Long productId) {
        ProductScoreDTO productScore = evaluationService.calculateProductScore(productId);
        return ResultVO.success(productScore);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（复用系统JWT解析逻辑，实际项目中需对接权限工具类）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 实际实现需从请求头Authorization中获取token并解析，此处为示例占位
        return 1L;
    }
}