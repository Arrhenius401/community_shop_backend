package com.community_shop.backend.controller;

import com.community_shop.backend.annotation.LoginRequired;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.order.*;
import com.community_shop.backend.service.base.OrderService;
import com.community_shop.backend.utils.RequestParseUtil;
import com.community_shop.backend.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理模块Controller，负责订单创建、状态流转、查询及支付回调等接口实现
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "订单管理接口", description = "包含订单创建、取消、支付回调、发货收货及订单查询等功能")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 创建订单接口
     * @param orderCreateDTO 订单创建请求参数（商品ID、购买数量、收货地址等）
     * @return 包含订单详情的统一响应
     */
    @PostMapping("/create")
    @LoginRequired
    @Operation(
            summary = "创建订单接口",
            description = "根据商品信息创建新订单，需校验库存与用户信用分，登录后访问"
    )
    public ResultVO<OrderDetailDTO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long currentUserId = parseUserIdFromToken();
        OrderDetailDTO orderDetail = orderService.createOrder(currentUserId, orderCreateDTO);
        return ResultVO.success(orderDetail);
    }

    /**
     * 取消订单接口
     * @param orderId 目标订单ID
     * @return 取消结果的统一响应
     */
    @PatchMapping("/{orderId}/cancel")
    @LoginRequired
    @Operation(
            summary = "取消订单接口",
            description = "取消待支付状态的订单，自动恢复商品库存，登录后访问"
    )
    public ResultVO<Boolean> cancelOrder(@PathVariable Long orderId) {
        Long currentUserId = parseUserIdFromToken();
        Boolean cancelResult = orderService.cancelOrder(currentUserId, orderId);
        return ResultVO.success(cancelResult);
    }

    /**
     * 支付回调处理接口
     * @param payCallbackDTO 支付平台回调参数（订单号、支付金额、签名等）
     * @return 回调处理结果（支付平台要求格式）
     */
    @PostMapping("/pay/callback")
    @Operation(
            summary = "支付回调处理接口",
            description = "接收支付平台回调通知，更新订单支付状态，无需登录"
    )
    public ResultVO<String> handlePayCallback(@Valid @RequestBody PayCallbackDTO payCallbackDTO) {
        String callbackResult = orderService.handlePayCallback(payCallbackDTO);
        return ResultVO.success(callbackResult);
    }

    /**
     * 卖家发货接口
     * @param orderId 目标订单ID
     * @param orderShipDTO 发货信息参数（物流单号、物流公司等）
     * @return 包含发货后订单详情的统一响应
     */
    @PatchMapping("/{orderId}/ship")
    @LoginRequired
    @Operation(
            summary = "卖家发货接口",
            description = "卖家更新订单发货状态，需校验卖家身份，登录后访问"
    )
    public ResultVO<OrderDetailDTO> shipOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderShipDTO orderShipDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        OrderDetailDTO updatedOrder = orderService.shipOrder(currentSellerId, orderId, orderShipDTO);
        return ResultVO.success(updatedOrder);
    }

    /**
     * 买家确认收货接口
     * @param orderId 目标订单ID
     * @return 确认收货结果的统一响应
     */
    @PatchMapping("/{orderId}/receive")
    @LoginRequired
    @Operation(
            summary = "买家确认收货接口",
            description = "买家确认收到商品，更新订单状态为已完成，登录后访问"
    )
    public ResultVO<OrderDetailDTO> confirmReceive(@PathVariable Long orderId) {
        Long currentBuyerId = parseUserIdFromToken();
        OrderDetailDTO receiveResult = orderService.confirmReceive(currentBuyerId, orderId);
        return ResultVO.success(receiveResult);
    }

    /**
     * 获取订单详情接口
     * @param orderId 目标订单ID
     * @return 包含订单完整信息的统一响应
     */
    @GetMapping("/{orderId}")
    @LoginRequired
    @Operation(
            summary = "获取订单详情接口",
            description = "查询指定订单的完整信息，需校验订单归属权，登录后访问"
    )
    public ResultVO<OrderDetailDTO> getOrderDetail(@PathVariable Long orderId) {
        Long currentUserId = parseUserIdFromToken();
        OrderDetailDTO orderDetail = orderService.getOrderDetail(currentUserId, orderId);
        return ResultVO.success(orderDetail);
    }

    /**
     * 买家查询订单列表接口
     * @param orderQueryDTO 订单查询参数（状态、时间范围、分页信息等）
     * @return 包含分页订单列表的统一响应
     */
    @GetMapping("/buyer/list")
    @LoginRequired
    @Operation(
            summary = "买家查询订单列表接口",
            description = "查询当前登录买家的订单列表，支持状态筛选与分页，登录后访问"
    )
    public ResultVO<PageResult<OrderListItemDTO>> getBuyerOrders(@Valid @ModelAttribute OrderQueryDTO orderQueryDTO) {
        Long currentBuyerId = parseUserIdFromToken();
        PageResult<OrderListItemDTO> orderList = orderService.getBuyerOrders(currentBuyerId, orderQueryDTO);
        return ResultVO.success(orderList);
    }

    /**
     * 卖家查询订单列表接口
     * @param sellerOrderQueryDTO 卖家订单查询参数（状态、买家信息、分页信息等）
     * @return 包含分页订单列表的统一响应
     */
    @GetMapping("/seller/list")
    @LoginRequired
    @Operation(
            summary = "卖家查询订单列表接口",
            description = "查询当前登录卖家的订单列表，支持多条件筛选，登录后访问"
    )
    public ResultVO<PageResult<OrderListItemDTO>> getSellerOrders(@Valid @ModelAttribute OrderQueryDTO sellerOrderQueryDTO) {
        Long currentSellerId = parseUserIdFromToken();
        PageResult<OrderListItemDTO> orderList = orderService.getSellerOrders(currentSellerId, sellerOrderQueryDTO);
        return ResultVO.success(orderList);
    }

    /**
     * 工具方法：从请求头令牌中解析用户ID（复用系统JWT解析逻辑）
     * @return 当前登录用户ID
     */
    private Long parseUserIdFromToken() {
        // 通过HttpServletRequest获取Authorization头，解析JWT令牌得到用户ID
        return requestParseUtil.parseUserIdFromRequest();
    }
}