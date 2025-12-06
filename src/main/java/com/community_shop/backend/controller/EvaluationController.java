package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.evaluation.*;
import com.community_shop.backend.service.base.EvaluationService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(
        name = "评价管理接口",
        description = "包含评价提交、编辑、删除、订单评价查询、商品评价列表及卖家/商品评分统计等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class EvaluationController {
    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 提交订单评价接口
     * 对应Service层：EvaluationServiceImpl.submitEvaluation()，校验订单已完成、未评价、评分1-5星
     */
    @PostMapping("/submit")
    @LoginRequired
    @Operation(
            summary = "提交订单评价接口",
            description = "买家对已完成订单提交评价，业务规则：1.仅订单买家可操作；2.订单需为已完成（COMPLETED）状态；3.订单未提交过评价；4.评分1-5星非空；5.评价内容1-500字非空；6.图片最多5张；7.好评（4-5星）+卖家信用分5分，差评（1-2星）-10分，中评（3星）不调整；8.提交后清除卖家评分缓存",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功，返回评价详情（含商品/评价人脱敏信息）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002、评分1-5星=EVAL_021、内容为空=EVAL_003、内容超500字=EVAL_022、图片超5张=EVAL_023）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非订单买家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（ORDER_001）/用户不存在（USER_051）/商品不存在（PRODUCT_001）/卖家不存在（MSG_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "订单未完成（ORDER_005）/订单已评价（EVAL_091）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（对应错误码：SYSTEM_013）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<EvaluationDetailDTO> submitEvaluation(
            @Valid @RequestBody
            @Parameter(description = "评价创建参数，orderId/score/content为必填，imageUrls/tags可选", required = true)
            EvaluationCreateDTO evaluationCreateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        EvaluationDetailDTO evaluationDetail = evaluationService.submitEvaluation(currentUserId, evaluationCreateDTO);
        return ResultVO.success(evaluationDetail);
    }

    /**
     * 编辑评价内容接口
     * 对应Service层：EvaluationServiceImpl.updateEvaluationContent()，校验评价归属权、内容非空
     */
    @PutMapping("/update")
    @LoginRequired
    @Operation(
            summary = "编辑评价内容接口",
            description = "评价人修改自身提交的评价内容，业务规则：1.仅评价创建者可操作；2.评价需存在且未删除；3.新内容1-500字非空；4.编辑后重新调整卖家信用分（按新内容关联评分）；5.清除卖家评分缓存",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "编辑成功，返回true",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（评价ID为空=EVAL_002、新内容为空=EVAL_003、新内容超500字=EVAL_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非评价创建者，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "评价不存在（对应错误码：EVAL_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> updateEvaluationContent(
            @Valid @RequestBody
            @Parameter(description = "评价更新参数，evalId/newContent为必填", required = true)
            EvaluationUpdateDTO evaluationUpdateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        Boolean updatedEvaluation = evaluationService.updateEvaluationContent(currentUserId, evaluationUpdateDTO);
        return ResultVO.success(updatedEvaluation);
    }

    /**
     * 删除评价接口
     * 对应Service层：EvaluationServiceImpl.deleteEvaluationById()，支持评价人/管理员删除，联动信用分
     */
    @DeleteMapping("/{evalId}")
    @LoginRequired
    @Operation(
            summary = "删除评价接口",
            description = "评价人或管理员删除评价，业务规则：1.评价人可删除自己的评价；2.管理员可删除所有评价；3.评价需存在；4.删除后重新计算卖家信用分（扣减原评分影响）；5.清除卖家评分缓存；6.删除为逻辑删除（保留数据记录）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功，返回true",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（评价ID为空=EVAL_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非评价人/非管理员，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "评价不存在（EVAL_001）/操作人不存在（USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据删除失败（对应错误码：SYSTEM_012）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> deleteEvaluationById(
            @PathVariable
            @Parameter(description = "目标评价ID", required = true, example = "7001")
            Long evalId
    ) {
        Long currentOperatorId = parseUserIdFromToken();
        Boolean deleteResult = evaluationService.deleteEvaluationById(evalId, currentOperatorId);
        return ResultVO.success(deleteResult);
    }

    /**
     * 获取订单评价详情接口
     * 对应Service层：EvaluationServiceImpl.getEvaluationByOrderId()，校验订单关联权限
     */
    @GetMapping("/order/{orderId}")
    @LoginRequired
    @Operation(
            summary = "获取订单评价详情接口",
            description = "查询指定订单的评价信息，业务规则：1.仅订单买家/卖家可查看；2.订单需存在；3.返回评价完整信息（含评价人脱敏信息、商品信息）；4.无评价时返回null",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回评价详情（无评价时返回null）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非订单买家/卖家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（ORDER_001）/关联数据缺失（SYSTEM_005）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<EvaluationDetailDTO> getEvaluationByOrderId(
            @PathVariable
            @Parameter(description = "关联订单ID", required = true, example = "5001")
            Long orderId
    ) {
        Long currentUserId = parseUserIdFromToken();
        EvaluationDetailDTO evaluationDetail = evaluationService.getEvaluationByOrderId(orderId);
        return ResultVO.success(evaluationDetail);
    }

    /**
     * 查询商品评价列表接口
     * 对应Service层：EvaluationServiceImpl.searchEvaluationsByQuery()，支持评分筛选，无需登录
     */
    @GetMapping("/product/{productId}")
    @Operation(
            summary = "查询商品评价列表接口",
            description = "查询指定商品的所有评价，业务规则：1.无需登录，公开访问；2.支持按评分范围（1-5星）筛选；3.分页默认pageNum=1、pageSize=10；4.默认按创建时间降序排序；5.评价人用户名脱敏（如“张***”）；6.内容摘要截取前100字"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页评价列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空=PRODUCT_004、评分范围非法=EVAL_021、分页参数为负数=SYSTEM_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<EvaluationListItemDTO>> searchProductEvaluations(
            @PathVariable
            @Parameter(description = "目标商品ID", required = true, example = "4001")
            Long productId,
            @Valid @ModelAttribute
            @Parameter(description = "评价查询参数，支持评分范围筛选及分页，productId自动填充")
            EvaluationQueryDTO evaluationQueryDTO
    ) {
        evaluationQueryDTO.setProductId(productId);
        PageResult<EvaluationListItemDTO> evaluationList = evaluationService.searchEvaluationsByQuery(evaluationQueryDTO);
        return ResultVO.success(evaluationList);
    }

    /**
     * 统计卖家评分接口
     * 对应Service层：EvaluationServiceImpl.calculateSellerScore()，计算平均分、好评率，无需登录
     */
    @GetMapping("/seller/{sellerId}/score")
    @Operation(
            summary = "统计卖家评分接口",
            description = "统计指定卖家的评分数据，业务规则：1.无需登录，公开访问；2.返回数据含：平均分（保留1位小数）、好评率（4-5星占比）、差评率（1-2星占比）、各星级评价数量、总评价数；3.无评价时所有数值为0"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功，返回卖家评分数据",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（卖家ID为空/≤0=USER_015）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "卖家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<SellerScoreDTO> calculateSellerScore(
            @PathVariable
            @Parameter(description = "目标卖家ID", required = true, example = "1001")
            Long sellerId
    ) {
        SellerScoreDTO sellerScore = evaluationService.calculateSellerScore(sellerId);
        return ResultVO.success(sellerScore);
    }

    /**
     * 统计商品评分接口
     * 对应Service层：EvaluationServiceImpl.calculateProductScore()，计算平均分、各星级数量，无需登录
     */
    @GetMapping("/product/{productId}/score")
    @Operation(
            summary = "统计商品评分接口",
            description = "统计指定商品的评分数据，业务规则：1.无需登录，公开访问；2.返回数据含：平均分（保留1位小数）、好评率（4-5星占比）、差评率（1-2星占比）、各星级评价数量、总评价数；3.无评价时所有数值为0"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功，返回商品评分数据",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空/≤0=PRODUCT_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "商品不存在（对应错误码：PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<ProductScoreDTO> calculateProductScore(
            @PathVariable
            @Parameter(description = "目标商品ID", required = true, example = "4001")
            Long productId
    ) {
        ProductScoreDTO productScore = evaluationService.calculateProductScore(productId);
        return ResultVO.success(productScore);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（复用系统JWT解析逻辑，实际项目中需对接权限工具类）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 实际实现需从请求头Authorization中获取token并解析，此处为示例占位（需替换为真实工具类调用）
        return requestParseUtil.parseUserIdFromRequest();
    }
}