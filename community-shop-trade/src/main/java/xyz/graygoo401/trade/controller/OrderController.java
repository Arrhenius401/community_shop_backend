package xyz.graygoo401.trade.controller;

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
import xyz.graygoo401.api.trade.dto.order.*;
import xyz.graygoo401.common.annotation.LoginRequired;
import xyz.graygoo401.common.dto.PageResult;
import xyz.graygoo401.common.util.RequestParseUtil;
import xyz.graygoo401.common.vo.ResultVO;
import xyz.graygoo401.trade.service.base.OrderService;


/**
 * 订单管理模块Controller，负责订单创建、状态流转、查询及支付回调等接口实现
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(
        name = "订单管理接口",
        description = "包含订单创建、取消、支付回调、卖家发货、买家确认收货及订单查询等功能，所有接口均返回统一ResultVO格式，错误场景关联ErrorCode枚举"
)
@Validated
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RequestParseUtil requestParseUtil;

    /**
     * 创建订单接口
     * 对应Service层：OrderServiceImpl.createOrder()，校验买家信用分≥60、商品库存、金额一致性
     */
    @PostMapping("/create")
    @LoginRequired
    @Operation(
            summary = "创建订单接口",
            description = "买家创建订单，业务规则：1.买家信用分需≥60分（低于无法下单）；2.商品需在售且库存≥购买数量；3.订单金额需与商品单价×数量一致（误差≤0.01元）；4.收货地址/支付方式不能为空；5.创建后默认状态为待支付（PENDING_PAYMENT），支付有效期30分钟",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功，返回订单详情（含订单编号、支付链接）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（商品ID为空=PRODUCT_004、数量≤0=SYSTEM_002、金额异常=ORDER_021、地址为空=SYSTEM_003）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "信用分不足（<60分，对应错误码：USER_081）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在（USER_051）/商品不存在（PRODUCT_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "409", description = "商品已下架（PRODUCT_091）/库存不足（PRODUCT_092）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据插入失败（SYSTEM_013）/库存更新失败（SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<OrderDetailDTO> createOrder(
            @Valid @RequestBody
            @Parameter(description = "订单创建参数，productId/quantity/totalAmount/address/payType为必填", required = true)
            OrderCreateDTO orderCreateDTO
    ) {
        Long currentUserId = parseUserIdFromToken();
        OrderDetailDTO orderDetail = orderService.createOrder(currentUserId, orderCreateDTO);
        return ResultVO.success(orderDetail);
    }

    /**
     * 取消订单接口
     * 对应Service层：OrderServiceImpl.cancelOrder()，校验待支付状态、订单归属权，自动恢复库存
     */
    @PatchMapping("/{orderId}/cancel")
    @LoginRequired
    @Operation(
            summary = "取消订单接口",
            description = "取消待支付状态订单，业务规则：1.仅待支付（PENDING_PAYMENT）订单可取消；2.仅订单买家或管理员可操作；3.取消后自动恢复商品库存；4.发送取消通知给卖家",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002、非待支付状态=ORDER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非订单买家/管理员，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（对应错误码：ORDER_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "库存恢复失败（SYSTEM_011）/订单状态更新失败（SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<Boolean> cancelOrder(
            @PathVariable
            @Parameter(description = "目标订单ID，需为整数", required = true, example = "5001")
            Long orderId
    ) {
        Long currentUserId = parseUserIdFromToken();
        Boolean cancelResult = orderService.cancelOrder(currentUserId, orderId);
        return ResultVO.success(cancelResult);
    }

    /**
     * 支付回调处理接口
     * 对应Service层：OrderServiceImpl.handlePayCallback()，校验签名、订单状态、金额一致性
     */
    @PostMapping("/pay/callback")
    @Operation(
            summary = "支付回调处理接口",
            description = "接收支付平台回调通知，业务规则：1.校验回调参数完整性（订单号/支付金额/签名不能为空）；2.验证签名合法性（防止伪造回调）；3.仅待支付订单可处理；4.支付金额与订单金额误差≤0.01元；5.处理成功返回\"success\"，失败返回\"fail:原因\""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "回调处理成功，返回\"success\"",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数不完整/签名验证失败/金额不匹配/订单状态异常",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（对应错误码：ORDER_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "订单支付状态更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<String> handlePayCallback(
            @Valid @RequestBody
            @Parameter(description = "支付回调参数，orderNo/payAmount/sign为必填，payNo可选", required = true)
            PayCallbackDTO payCallbackDTO
    ) {
        String callbackResult = orderService.handlePayCallback(payCallbackDTO);
        return ResultVO.success(callbackResult);
    }

    /**
     * 卖家发货接口
     * 对应Service层：OrderServiceImpl.shipOrder()，校验卖家身份、待发货状态，更新物流信息
     */
    @PatchMapping("/{orderId}/ship")
    @LoginRequired
    @Operation(
            summary = "卖家发货接口",
            description = "卖家更新订单发货状态，业务规则：1.仅订单所属卖家可操作；2.仅待发货（PENDING_SHIPMENT）订单可发货；3.物流公司/物流单号不能为空；4.发货后状态更新为已发货（SHIPPED），发送通知给买家",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发货成功，返回更新后订单详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002、物流公司/单号为空=SYSTEM_003、非待发货状态=ORDER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非订单所属卖家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（对应错误码：ORDER_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "订单发货状态更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<OrderDetailDTO> shipOrder(
            @PathVariable
            @Parameter(description = "目标订单ID", required = true, example = "5001")
            Long orderId,
            @Valid @RequestBody
            @Parameter(description = "发货信息参数，expressCompany/expressNo为必填", required = true)
            OrderShipDTO orderShipDTO
    ) {
        Long currentSellerId = parseUserIdFromToken();
        OrderDetailDTO updatedOrder = orderService.shipOrder(currentSellerId, orderId, orderShipDTO);
        return ResultVO.success(updatedOrder);
    }

    /**
     * 买家确认收货接口
     * 对应Service层：OrderServiceImpl.confirmReceive()，校验买家身份、已发货状态
     */
    @PatchMapping("/{orderId}/receive")
    @LoginRequired
    @Operation(
            summary = "买家确认收货接口",
            description = "买家确认收到商品，业务规则：1.仅订单所属买家可操作；2.仅已发货（SHIPPED）订单可确认；3.确认后状态更新为已完成（COMPLETED），发送通知给卖家",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "确认收货成功，返回更新后订单详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002、非已发货状态=ORDER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限（非订单所属买家，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（对应错误码：ORDER_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "订单状态更新失败（对应错误码：SYSTEM_011）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<OrderDetailDTO> confirmReceive(
            @PathVariable
            @Parameter(description = "目标订单ID", required = true, example = "5001")
            Long orderId
    ) {
        Long currentBuyerId = parseUserIdFromToken();
        OrderDetailDTO receiveResult = orderService.confirmReceive(currentBuyerId, orderId);
        return ResultVO.success(receiveResult);
    }

    /**
     * 获取订单详情接口
     * 对应Service层：OrderServiceImpl.getOrderDetail()，校验订单归属权，优先从缓存获取
     */
    @GetMapping("/{orderId}")
    @LoginRequired
    @Operation(
            summary = "获取订单详情接口",
            description = "查询指定订单完整信息，业务规则：1.仅订单买家/卖家/管理员可查看；2.返回信息含商品缩略信息、买卖家脱敏信息、物流信息；3.优先从缓存获取，缓存有效期30分钟",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回订单完整详情",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（订单ID为空=ORDER_002）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "403", description = "无权限查看（非订单关联方，对应错误码：SYSTEM_022）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "订单不存在（对应错误码：ORDER_001）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<OrderDetailDTO> getOrderDetail(
            @PathVariable
            @Parameter(description = "目标订单ID", required = true, example = "5001")
            Long orderId
    ) {
        Long currentUserId = parseUserIdFromToken();
        OrderDetailDTO orderDetail = orderService.getOrderDetail(currentUserId, orderId);
        return ResultVO.success(orderDetail);
    }

    /**
     * 买家查询订单列表接口
     * 对应Service层：OrderServiceImpl.getBuyerOrders()，仅查询当前买家订单，支持状态筛选
     */
    @GetMapping("/buyer/list")
    @LoginRequired
    @Operation(
            summary = "买家查询订单列表接口",
            description = "查询当前登录买家的订单列表，业务规则：1.仅返回当前买家的订单；2.支持按订单状态筛选（待支付/待发货/已发货/已完成/已取消）；3.分页默认pageNum=1、pageSize=10；4.默认按创建时间降序排序，结果优先从缓存获取（有效期15分钟）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页订单列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（分页参数为负数=SYSTEM_002、状态非法=ORDER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "买家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<OrderListItemDTO>> getBuyerOrders(
            @Valid @ModelAttribute
            @Parameter(description = "买家订单查询参数，支持状态筛选及分页，buyerId自动从Token解析")
            OrderQueryDTO orderQueryDTO
    ) {
        Long currentBuyerId = parseUserIdFromToken();
        PageResult<OrderListItemDTO> orderList = orderService.getBuyerOrders(currentBuyerId, orderQueryDTO);
        return ResultVO.success(orderList);
    }

    /**
     * 卖家查询订单列表接口
     * 对应Service层：OrderServiceImpl.getSellerOrders()，仅查询当前卖家订单，支持多条件筛选
     */
    @GetMapping("/seller/list")
    @LoginRequired
    @Operation(
            summary = "卖家查询订单列表接口",
            description = "查询当前登录卖家的订单列表，业务规则：1.仅返回当前卖家的订单；2.支持按订单状态（待支付/待发货/已发货等）筛选；3.分页默认pageNum=1、pageSize=10；4.默认按创建时间降序排序，结果优先从缓存获取（有效期15分钟）",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功，返回分页订单列表（无数据时列表为空）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（分页参数为负数=SYSTEM_002、状态非法=ORDER_004）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "401", description = "未登录（对应错误码：SYSTEM_021）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "404", description = "卖家不存在（对应错误码：USER_051）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class))),
            @ApiResponse(responseCode = "500", description = "数据查询失败（对应错误码：SYSTEM_014）",
                    content = @Content(schema = @Schema(implementation = ResultVO.class)))
    })
    public ResultVO<PageResult<OrderListItemDTO>> getSellerOrders(
            @Valid @ModelAttribute
            @Parameter(description = "卖家订单查询参数，支持状态筛选及分页，sellerId自动从Token解析")
            OrderQueryDTO sellerOrderQueryDTO
    ) {
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