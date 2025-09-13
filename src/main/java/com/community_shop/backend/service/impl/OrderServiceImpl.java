package com.community_shop.backend.service.impl;

import com.community_shop.backend.convert.OrderConvert;
import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.message.MessageSendDTO;
import com.community_shop.backend.dto.order.*;
import com.community_shop.backend.dto.product.ProductStockUpdateDTO;
import com.community_shop.backend.enums.CodeEnum.MessageTypeEnum;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.CodeEnum.ProductStatusEnum;
import com.community_shop.backend.enums.CodeEnum.UserRoleEnum;
import com.community_shop.backend.enums.ErrorCode.ErrorCode;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.OrderMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.OrderService;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.service.base.UserService;
import com.community_shop.backend.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现类，实现订单创建、支付、查询等核心业务逻辑
 * 依赖UserService校验用户信用、ProductService处理库存、MessageService发送通知
 */
@Slf4j
@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements OrderService {

    // 订单相关常量
    private static final Integer MIN_BUYER_CREDIT_SCORE = 60; // 买家创建订单最低信用分
    private static final String ORDER_NO_PREFIX = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")); // 订单编号前缀（yyyyMMdd）
    private static final int ORDER_NO_RANDOM_LEN = 8; // 订单编号随机数长度
    private static final int PAY_EXPIRE_MINUTES = 30; // 支付有效期（分钟）

    // 缓存相关常量
    private static final String CACHE_KEY_ORDER = "order:info:"; // 订单信息缓存Key前缀
    private static final String CACHE_KEY_ORDER_LIST = "order:list:"; // 订单列表缓存Key前缀
    private static final String CACHE_KEY_USER_ORDERS = "order:user:"; // 用户订单列表缓存Key前缀
    private static final long CACHE_TTL_ORDER = 30; // 订单缓存有效期（分钟）
    private static final long CACHE_TTL_ORDER_LIST = 15; // 订单列表缓存有效期（分钟）

    // 信用分最低要求（下单）
    private static final Integer MIN_CREDIT_FOR_ORDER = 60;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private OrderConvert orderConvert;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private OrderService orderService;


    /**
     * 创建订单（支持单商品）
     * 核心逻辑：参数校验→买家信用校验→商品库存校验→创建订单→扣减库存→生成支付信息→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OrderDetailDTO createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        try {
            // 1. 基础参数校验
            validateOrderCreateParam(orderCreateDTO);

            // 2. 买家信息与信用校验
            User buyer = userService.getById(userId);
            if (buyer == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }
            if (buyer.getCreditScore() < MIN_BUYER_CREDIT_SCORE) {
                throw new BusinessException(ErrorCode.CREDIT_TOO_LOW,
                        "买家信用分不足" + MIN_BUYER_CREDIT_SCORE + "分，无法创建订单");
            }

            // 3. 商品信息与库存校验
            Product product = productService.getById(orderCreateDTO.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }
            if (!ProductStatusEnum.ON_SALE.equals(product.getStatus())) {
                throw new BusinessException(ErrorCode.PRODUCT_ALREADY_OFF_SALE, "商品已下架或不可售");
            }
            if (product.getStock() < orderCreateDTO.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT,
                        "商品库存不足，当前库存：" + product.getStock());
            }
            // 金额一致性校验（防止前端篡改）
            BigDecimal actualAmount = product.getPrice().multiply(BigDecimal.valueOf(orderCreateDTO.getQuantity()));
            if ((actualAmount.subtract(orderCreateDTO.getTotalAmount()).abs().compareTo(BigDecimal.valueOf(0.01))) < 0) {
                throw new BusinessException(ErrorCode.ORDER_AMOUNT_ABNORMAL, "订单金额异常，请重新下单");
            }

            // 4. 构建订单实体
            Order order = orderConvert.orderCreateDtoToOrder(orderCreateDTO);
            order.setBuyerId(userId);
            order.setSellerId(product.getSellerId());
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT); // 初始状态：待支付
            order.setCreateTime(LocalDateTime.now());
            order.setOrderNo(generateOrderNo()); // 生成唯一订单编号

            // 5. 执行数据库操作（创建订单 + 扣减库存）
            int orderInsertCount = orderMapper.insert(order);
            if (orderInsertCount != 1) {
                log.error("订单创建失败，订单信息：{}", order);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED, "订单创建失败，请重试");
            }

            ProductStockUpdateDTO stockUpdateDTO = new ProductStockUpdateDTO(product.getProductId(),
                    -orderCreateDTO.getQuantity(), "订单创建");
            int stockUpdateCount = productService.updateStock(product.getSellerId(), stockUpdateDTO);
            if (stockUpdateCount != 1) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED, "库存更新失败，订单创建回滚");
            }

            // 6. 生成支付信息（模拟支付链接，实际项目对接第三方支付接口）
            OrderDetailDTO orderDetail = orderConvert.orderToOrderDetailDTO(order);
//            orderDetail.setPayUrl(generatePayUrl(order.getOrderNo(), orderCreateDTO.getPayType()));
//            orderDetail.setPayExpireTime(LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES));

            // 7. 缓存订单详情
            redisTemplate.opsForValue().set(
                    CACHE_KEY_ORDER + order.getOrderId(),
                    orderDetail,
                    CACHE_TTL_ORDER,
                    TimeUnit.MINUTES
            );

            // 8. 发送订单创建通知给卖家
            sendSellerOrderNotice(order.getSellerId(), order.getOrderId(), "新订单通知",
                    "您有一笔新订单，订单编号：" + order.getOrderNo() + "，商品：" + product.getTitle());

            log.info("订单创建成功，订单ID：{}，买家ID：{}", order.getOrderId(), userId);
            return orderDetail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建订单异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "订单创建异常，请联系客服");
        }
    }

    /**
     * 取消订单
     * 核心逻辑：参数校验→订单存在性校验→权限校验→状态校验→恢复库存→更新订单状态→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Boolean cancelOrder(Long userId, Long orderId) {
        try {
            // 1. 基础参数校验
            if (userId == null || orderId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "用户ID和订单ID不能为空");
            }

            // 2. 订单存在性校验（优先查缓存）
            OrderDetailDTO cacheOrder = (OrderDetailDTO) redisTemplate.opsForValue().get(CACHE_KEY_ORDER + orderId);
            Order order = cacheOrder != null ? orderMapper.selectById(cacheOrder.getOrderId()) : orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 3. 权限校验（买家或管理员可取消）
            User operator = userService.getById(userId);
            boolean isBuyer = Objects.equals(order.getBuyerId(), userId);
            boolean isAdmin = operator != null && UserRoleEnum.ADMIN.equals(operator.getRole());
            if (!isBuyer && !isAdmin) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权限取消此订单");
            }

            // 4. 订单状态校验（仅待支付状态可取消）
            if (!OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID,
                        "订单状态为【" + order.getStatus().getDesc() + "】，不允许取消");
            }

            // 5. 恢复商品库存
            Product product = productService.getById(order.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS, "商品不存在，无法恢复库存");
            }
            ProductStockUpdateDTO stockUpdateDTO = new ProductStockUpdateDTO(product.getProductId(),
                    order.getQuantity(), "订单创建");
            int stockUpdateCount = productService.updateStock(product.getSellerId(), stockUpdateDTO);
            if (stockUpdateCount != 1) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED, "库存恢复失败，取消订单回滚");
            }

            // 6. 更新订单状态为已取消
            order.setStatus(OrderStatusEnum.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
//            order.setCancelReason(isBuyer ? "买家主动取消" : "管理员操作取消");
            int orderUpdateCount = orderMapper.updateById(order);
            if (orderUpdateCount != 1) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED, "订单状态更新失败");
            }

            // 7. 清除缓存
            redisTemplate.delete(CACHE_KEY_ORDER + orderId);
            // 清除订单列表缓存（买家和卖家）
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "buyer:" + order.getBuyerId());
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "seller:" + order.getSellerId());

            // 8. 发送取消通知给卖家
            sendSellerOrderNotice(order.getSellerId(), orderId, "订单取消通知",
                    "订单编号：" + order.getOrderNo() + " 已取消，库存已恢复");

            log.info("订单取消成功，订单ID：{}，操作人ID：{}", orderId, userId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消订单异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单异常，请联系客服");
        }
    }

    /**
     * 支付订单回调处理
     * 核心逻辑：参数校验→签名校验→订单校验→状态校验→更新订单支付信息→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String handlePayCallback(PayCallbackDTO payCallbackDTO) {
        try {
            // 1. 基础参数校验
            if (payCallbackDTO == null || !StringUtils.hasText(payCallbackDTO.getOrderNo())
                    || payCallbackDTO.getPayAmount() == null || !StringUtils.hasText(payCallbackDTO.getSign())) {
                log.error("支付回调参数不完整，参数：{}", payCallbackDTO);
                return "fail:参数不完整";
            }

            // 2. 签名校验（防止回调伪造，使用系统统一签名密钥）
            boolean signValid = SignUtil.verifySign(payCallbackDTO, "ORDER_PAY_CALLBACK_SECRET");
            if (!signValid) {
                log.error("支付回调签名验证失败，订单号：{}", payCallbackDTO.getOrderNo());
                return "fail:签名验证失败";
            }

            // 3. 订单查询与校验
            Order order = orderMapper.selectByOrderNo(payCallbackDTO.getOrderNo());
            if (order == null) {
                log.error("支付回调订单不存在，订单号：{}", payCallbackDTO.getOrderNo());
                return "fail:订单不存在";
            }

            // 4. 订单状态校验（仅待支付状态可处理支付）
            if (!OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus())) {
                log.error("支付回调订单状态异常，订单号：{}，当前状态：{}",
                        payCallbackDTO.getOrderNo(), order.getStatus().name());
                return "fail:订单状态异常，当前状态：" + order.getStatus().getDesc();
            }

            // 5. 支付金额校验
            if (order.getTotalAmount().subtract(payCallbackDTO.getPayAmount()).abs().compareTo(new BigDecimal("0.01")) > 0) {
                log.error("支付回调金额不匹配，订单号：{}，订单金额：{}，支付金额：{}",
                        payCallbackDTO.getOrderNo(), order.getTotalAmount(), payCallbackDTO.getPayAmount());
                return "fail:支付金额与订单金额不一致";
            }

            // 6. 更新订单支付信息
            order.setStatus(OrderStatusEnum.PAID);
            order.setPayTime(LocalDateTime.now());
            order.setPayType(PayTypeEnum.valueOf(payCallbackDTO.getPayType()));
//            order.setPayNo(payCallbackDTO.getPayNo()); // 第三方支付流水号
            int updateCount = orderMapper.updateById(order);
            if (updateCount != 1) {
                log.error("支付回调订单状态更新失败，订单号：{}", payCallbackDTO.getOrderNo());
                return "fail:订单支付状态更新失败";
            }

            // 7. 清除缓存
            redisTemplate.delete(CACHE_KEY_ORDER + order.getOrderId());
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "buyer:" + order.getBuyerId());
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "seller:" + order.getSellerId());

            // 8. 发送支付成功通知（买家 + 卖家）
            sendBuyerOrderNotice(order.getBuyerId(), order.getOrderId(), "支付成功通知",
                    "您的订单（编号：" + order.getOrderNo() + "）已支付成功，等待卖家发货");
            sendSellerOrderNotice(order.getSellerId(), order.getOrderId(), "订单支付通知",
                    "订单编号：" + order.getOrderNo() + " 已支付，请及时发货");

            log.info("支付回调处理成功，订单号：{}，支付流水号：{}",
                    payCallbackDTO.getOrderNo(), payCallbackDTO.getPayNo());
            return "success";
        } catch (IllegalArgumentException e) {
            log.error("支付回调支付方式枚举转换失败，参数：{}", payCallbackDTO.getPayType());
            return "fail:支付方式非法";
        } catch (Exception e) {
            log.error("支付回调处理异常，参数：{}", payCallbackDTO, e);
            return "fail:系统异常，请重试";
        }
    }

    /**
     * 卖家发货
     * 核心逻辑：参数校验→订单校验→权限校验→状态校验→更新订单发货信息→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OrderDetailDTO shipOrder(Long sellerId, Long orderId, OrderShipDTO shipDTO) {
        try {
            // 1. 基础参数校验
            if (sellerId == null || orderId == null || shipDTO == null
                    || !StringUtils.hasText(shipDTO.getExpressCompany())
                    || !StringUtils.hasText(shipDTO.getExpressNo())) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "参数不完整，发货失败");
            }

            // 2. 订单校验（优先查缓存）
            OrderDetailDTO cacheOrder = (OrderDetailDTO) redisTemplate.opsForValue().get(CACHE_KEY_ORDER + orderId);
            Order order = cacheOrder != null ? orderMapper.selectById(cacheOrder.getOrderId()) : orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 3. 权限校验（仅订单所属卖家可发货）
            if (!Objects.equals(order.getSellerId(), sellerId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权限发货，此订单不属于当前卖家");
            }

            // 4. 订单状态校验（仅已支付状态可发货）
            if (!OrderStatusEnum.PAID.equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID,
                        "订单状态为【" + order.getStatus().getDesc() + "】，不允许发货");
            }

            // 5. 更新订单发货信息
            order.setStatus(OrderStatusEnum.SHIPPED);
            order.setShipTime(LocalDateTime.now());
//            order.setExpressCompany(shipDTO.getExpressCompany());
//            order.setExpressNo(shipDTO.getExpressNo());
//            order.setShipRemark(shipDTO.getShipRemark());
            int updateCount = orderMapper.updateById(order);
            if (updateCount != 1) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED, "订单发货状态更新失败");
            }

            // 6. 转换为DTO并补充商品信息
            OrderDetailDTO orderDetail = orderConvert.orderToOrderDetailDTO(order);
//            Product product = productMapper.selectById(order.getProductId());
//            if (product != null) {
//                orderDetail.setProductName(product.getTitle());
//                orderDetail.setProductImage(product.getImageUrls().length > 0 ? product.getImageUrls()[0] : "");
//            }

            // 7. 缓存更新后的订单详情
            redisTemplate.opsForValue().set(
                    CACHE_KEY_ORDER + orderId,
                    orderDetail,
                    CACHE_TTL_ORDER,
                    TimeUnit.MINUTES
            );
            // 清除订单列表缓存
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "buyer:" + order.getBuyerId());
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "seller:" + sellerId);

            // 8. 发送发货通知给买家
            sendBuyerOrderNotice(order.getBuyerId(), orderId, "卖家发货通知",
                    "您的订单（编号：" + order.getOrderNo() + "）已发货，快递公司："
                            + shipDTO.getExpressCompany() + "，快递单号：" + shipDTO.getExpressNo());

            log.info("订单发货成功，订单ID：{}，卖家ID：{}", orderId, sellerId);
            return orderDetail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("订单发货异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发货操作异常，请联系客服");
        }
    }

    /**
     * 买家确认收货
     * 核心逻辑：参数校验→订单校验→权限校验→状态校验→更新订单状态→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public OrderDetailDTO confirmReceive(Long buyerId, Long orderId) {
        try {
            // 1. 基础参数校验
            if (buyerId == null || orderId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "用户ID和订单ID不能为空");
            }

            // 2. 订单校验（优先查缓存）
            OrderDetailDTO cacheOrder = (OrderDetailDTO) redisTemplate.opsForValue().get(CACHE_KEY_ORDER + orderId);
            Order order = cacheOrder != null ? orderService.getById(cacheOrder.getOrderId()) : orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 3. 权限校验（仅订单所属买家可确认收货）
            if (!Objects.equals(order.getBuyerId(), buyerId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权限确认收货，此订单不属于当前买家");
            }

            // 4. 订单状态校验（仅已发货状态可确认收货）
            if (!OrderStatusEnum.SHIPPED.equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID,
                        "订单状态为【" + order.getStatus().getDesc() + "】，不允许确认收货");
            }

            // 5. 更新订单状态为已完成
            order.setStatus(OrderStatusEnum.COMPLETED);
            order.setReceiveTime(LocalDateTime.now());
            int updateCount = orderMapper.updateById(order);
            if (updateCount != 1) {
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED, "订单确认收货状态更新失败");
            }

            // 6. 转换为DTO并补充商品信息
            OrderDetailDTO orderDetail = orderConvert.orderToOrderDetailDTO(order);
//            Product product = productService.getById(order.getProductId());
//            if (product != null) {
//                orderDetail.setProductName(product.getTitle());
//                orderDetail.setProductImage(product.getImageUrls().length > 0 ? product.getImageUrls()[0] : "");
//            }

            // 7. 缓存更新后的订单详情
            redisTemplate.opsForValue().set(
                    CACHE_KEY_ORDER + orderId,
                    orderDetail,
                    CACHE_TTL_ORDER,
                    TimeUnit.MINUTES
            );
            // 清除订单列表缓存
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "buyer:" + buyerId);
            redisTemplate.delete(CACHE_KEY_ORDER_LIST + "seller:" + order.getSellerId());

            // 8. 发送确认收货通知给卖家
            sendSellerOrderNotice(order.getSellerId(), orderId, "买家确认收货通知",
                    "订单编号：" + order.getOrderNo() + " 买家已确认收货，交易完成");

            log.info("订单确认收货成功，订单ID：{}，买家ID：{}", orderId, buyerId);
            return orderDetail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("订单确认收货异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "确认收货操作异常，请联系客服");
        }
    }

    /**
     * 查询订单详情
     * 核心逻辑：参数校验→订单查询→权限校验→数据封装
     */
    @Override
    public OrderDetailDTO getOrderDetail(Long userId, Long orderId) {
        try {
            // 1. 基础参数校验
            if (userId == null || orderId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "用户ID和订单ID不能为空");
            }

            // 2. 优先查询缓存
            OrderDetailDTO orderDetail = (OrderDetailDTO) redisTemplate.opsForValue().get(CACHE_KEY_ORDER + orderId);
            if (orderDetail != null) {
                // 缓存中存在，先校验权限
                validateOrderPermission(userId, orderDetail.getBuyerId(), orderDetail.getSellerId());
                return orderDetail;
            }

            // 3. 数据库查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 4. 权限校验（买家/卖家/管理员可查看）
            validateOrderPermission(userId, order.getBuyerId(), order.getSellerId());

            // 5. 转换为DTO并补充商品信息
            orderDetail = orderConvert.orderToOrderDetailDTO(order);
//            Product product = productMapper.selectById(order.getProductId());
//            if (product != null) {
//                orderDetail.setProductName(product.getTitle());
//                orderDetail.setProductImage(product.getImageUrls().length > 0 ? product.getImageUrls()[0] : "");
//            }

            // 6. 缓存订单详情
            redisTemplate.opsForValue().set(
                    CACHE_KEY_ORDER + orderId,
                    orderDetail,
                    CACHE_TTL_ORDER,
                    TimeUnit.MINUTES
            );

            log.info("查询订单详情成功，订单ID：{}，操作人ID：{}", orderId, userId);
            return orderDetail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询订单详情异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED, "查询订单详情失败，请重试");
        }
    }

    /**
     * 买家查询订单列表
     * 核心逻辑：参数处理→缓存查询→数据库查询→数据转换→缓存更新
     */
    @Override
    public PageResult<OrderListItemDTO> getBuyerOrders(Long buyerId, OrderQueryDTO queryDTO) {
        try {
            // 1. 基础参数校验
            if (buyerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "买家ID不能为空");
            }
            if (queryDTO == null) {
                queryDTO = new OrderQueryDTO();
            }

            // 2. 处理分页参数（默认页码1，每页10条）
            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
            int offset = (pageNum - 1) * pageSize;

            // 3. 构建缓存Key（包含买家ID、状态、分页参数）
            String cacheKey = CACHE_KEY_ORDER_LIST + "buyer:" + buyerId
                    + ":status:" + (queryDTO.getStatus() == null ? "ALL" : queryDTO.getStatus().name())
                    + ":page:" + pageNum + ":size:" + pageSize;

            // 4. 优先查询缓存
            PageResult<OrderListItemDTO> cacheResult = (PageResult<OrderListItemDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cacheResult != null) {
                log.info("从缓存获取买家订单列表，买家ID：{}，页码：{}", buyerId, pageNum);
                return cacheResult;
            }

            // 5. 数据库查询（订单列表 + 总条数）
            List<Order> orderList = orderMapper.selectByBuyerId(buyerId, queryDTO.getStatus(), offset, pageSize);
            long total = orderMapper.countByBuyerId(buyerId, queryDTO.getStatus());

            // 6. 转换为订单列表DTO（补充商品缩略信息）
            List<OrderListItemDTO> listDTOs = orderList.stream().map(order -> {
                OrderListItemDTO listDTO = orderConvert.orderToOrderListItemDTO(order);
                Product product = productService.getById(order.getProductId());
//                if (product != null) {
//                    listDTO.setProductSummary(product.getTitle());
//                    listDTO.setProductImage(product.getImageUrls().length > 0 ? product.getImageUrls()[0] : "");
//                }
                return listDTO;
            }).collect(Collectors.toList());

            // 7. 封装分页结果
            long totalPages = (total + pageSize - 1) / pageSize;
            PageResult<OrderListItemDTO> result = new PageResult<>(
                    total, totalPages, listDTOs, pageNum, pageSize
            );

            // 8. 缓存订单列表
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_ORDER_LIST, TimeUnit.MINUTES);

            log.info("查询买家订单列表成功，买家ID：{}，总条数：{}，页码：{}", buyerId, total, pageNum);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询买家订单列表异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED, "查询订单列表失败，请重试");
        }
    }

    /**
     * 卖家查询订单列表
     * 核心逻辑：参数处理→缓存查询→数据库查询→数据转换→缓存更新
     */
    @Override
    public PageResult<OrderListItemDTO> getSellerOrders(Long sellerId, OrderQueryDTO queryDTO) {
        try {
            // 1. 基础参数校验
            if (sellerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL, "卖家ID不能为空");
            }
            if (queryDTO == null) {
                queryDTO = new OrderQueryDTO();
            }

            // 2. 处理分页参数（默认页码1，每页10条）
            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
            int offset = (pageNum - 1) * pageSize;

            // 3. 构建缓存Key（包含卖家ID、状态、分页参数）
            String cacheKey = CACHE_KEY_ORDER_LIST + "seller:" + sellerId
                    + ":status:" + (queryDTO.getStatus() == null ? "ALL" : queryDTO.getStatus().name())
                    + ":page:" + pageNum + ":size:" + pageSize;

            // 4. 优先查询缓存
            PageResult<OrderListItemDTO> cacheResult = (PageResult<OrderListItemDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cacheResult != null) {
                log.info("从缓存获取卖家订单列表，卖家ID：{}，页码：{}", sellerId, pageNum);
                return cacheResult;
            }

            // 5. 数据库查询（订单列表 + 总条数）
            List<Order> orderList = orderMapper.selectBySellerId(sellerId, queryDTO.getStatus(), offset, pageSize);
            long total = orderMapper.countBySellerId(sellerId, queryDTO.getStatus());

            // 6. 转换为订单列表DTO（补充商品缩略信息）
            List<OrderListItemDTO> listDTOs = orderList.stream().map(order -> {
                OrderListItemDTO listDTO = orderConvert.orderToOrderListItemDTO(order);
                Product product = productService.getById(order.getProductId());
//                if (product != null) {
//                    listDTO.setProductSummary(product.getTitle());
//                    listDTO.setProductImage(product.getImageUrls().length > 0 ? product.getImageUrls()[0] : "");
//                }
                return listDTO;
            }).collect(Collectors.toList());

            // 7. 封装分页结果
            long totalPages = (total + pageSize - 1) / pageSize;
            PageResult<OrderListItemDTO> result = new PageResult<>(
                    total, totalPages, listDTOs, pageNum, pageSize
            );

            // 8. 缓存订单列表
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_ORDER_LIST, TimeUnit.MINUTES);

            log.info("查询卖家订单列表成功，卖家ID：{}，总条数：{}，页码：{}", sellerId, total, pageNum);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询卖家订单列表异常", e);
            throw new BusinessException(ErrorCode.DATA_QUERY_FAILED, "查询订单列表失败，请重试");
        }
    }

    /**
     * 自动关闭超时未支付订单（定时任务调用）
     * 核心逻辑：查询超时订单→批量更新状态→恢复库存→发送通知
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public int autoCloseTimeoutOrders(int timeoutMinutes) {
        try {
            if (timeoutMinutes <= 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "超时时间必须大于0");
            }

            // 1. 计算超时时间点（当前时间 - 超时分钟数）
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

            // 2. 查询超时未支付订单
            List<Order> timeoutOrders = orderMapper.selectTimeoutPendingOrders(timeoutTime);
            if (timeoutOrders.isEmpty()) {
                log.info("无超时未支付订单，超时时间：{}分钟", timeoutMinutes);
                return 0;
            }

            // 3. 批量处理超时订单（更新状态 + 恢复库存）
            int closedCount = 0;
            for (Order order : timeoutOrders) {
                try {
                    // 3.1 更新订单状态为已取消
                    order.setStatus(OrderStatusEnum.CANCELLED);
                    order.setCancelTime(LocalDateTime.now());
//                    order.setCancelReason("超时未支付，系统自动关闭");
                    int updateCount = orderMapper.updateById(order);
                    if (updateCount != 1) {
                        log.error("自动关闭订单状态更新失败，订单ID：{}", order.getOrderId());
                        continue;
                    }

                    // 3.2 恢复商品库存
                    Product product = productService.getById(order.getProductId());
                    ProductStockUpdateDTO stockUpdateDTO = new ProductStockUpdateDTO(order.getProductId(),
                            order.getQuantity(), "订单超时关闭");
                    if (product != null) {
                        productService.updateStock(product.getProductId(), stockUpdateDTO);
                    } else {
                        log.warn("自动关闭订单商品不存在，无法恢复库存，订单ID：{}", order.getOrderId());
                    }

                    // 3.3 清除缓存
                    redisTemplate.delete(CACHE_KEY_ORDER + order.getOrderId());
                    redisTemplate.delete(CACHE_KEY_ORDER_LIST + "buyer:" + order.getBuyerId());
                    redisTemplate.delete(CACHE_KEY_ORDER_LIST + "seller:" + order.getSellerId());

                    // 3.4 发送系统关闭通知给买家
                    sendBuyerOrderNotice(order.getBuyerId(), order.getOrderId(), "订单超时关闭通知",
                            "您的订单（编号：" + order.getOrderNo() + "）因超时未支付，已被系统自动关闭，库存已恢复");

                    closedCount++;
                    log.info("自动关闭超时订单成功，订单ID：{}，订单编号：{}", order.getOrderId(), order.getOrderNo());
                } catch (Exception e) {
                    log.error("自动关闭订单异常，订单ID：{}", order.getOrderId(), e);
                    // 单个订单处理失败不影响其他订单
                    continue;
                }
            }

            log.info("自动关闭超时未支付订单完成，共处理{}个订单，成功关闭{}个", timeoutOrders.size(), closedCount);
            return closedCount;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("自动关闭超时订单批量处理异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "自动关闭订单异常，请联系运维");
        }
    }

    /**
     * 更新订单状态（基础CRUD）
     * 核心逻辑：校验状态流转合法性与操作用户权限，更新状态并刷新缓存
     */
    @Override
    public Boolean updateOrderStatus(Long orderId, OrderStatusEnum status, Long operatorId) {
        try {
            // 1. 参数校验
            if (orderId == null || status == null || operatorId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 查询订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 3. 校验状态流转合法性（示例：待支付→已取消、待支付→已支付等）
            validateOrderStatusTransition(order.getStatus(), status);

            // 4. 校验操作用户权限
            validateOrderOperatorPermission(order, status, operatorId);

            // 5. 更新订单状态
            int updateRows = orderMapper.updateStatus(orderId, status);
            if (updateRows <= 0) {
                log.error("更新订单状态失败，订单ID：{}，目标状态：{}", orderId, status);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }

            // 6. 刷新缓存（删除旧缓存，下次查询重新加载）
            redisTemplate.delete(CACHE_KEY_ORDER + orderId);
            redisTemplate.delete(CACHE_KEY_USER_ORDERS + order.getBuyerId()); // 删除买家订单列表缓存
            redisTemplate.delete(CACHE_KEY_USER_ORDERS + order.getSellerId()); // 删除卖家订单列表缓存

            log.info("更新订单状态成功，订单ID：{}，原状态：{}，新状态：{}，操作人：{}",
                    orderId, order.getStatus(), status, operatorId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新订单状态系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    // ---------------------- 私有辅助方法 ----------------------

    /**
     * 校验订单创建参数
     */
    private void validateOrderCreateParam(OrderCreateDTO orderCreateDTO) {
        if (orderCreateDTO == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "订单创建参数不能为空");
        }
        if (orderCreateDTO.getProductId() == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "商品ID不能为空");
        }
        if (orderCreateDTO.getQuantity() == null || orderCreateDTO.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "购买数量必须大于0");
        }
        if (orderCreateDTO.getTotalAmount() == null || orderCreateDTO.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单金额必须大于0");
        }
        if (!StringUtils.hasText(orderCreateDTO.getAddress())) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "收货地址不能为空");
        }
        if (orderCreateDTO.getPayType() == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "支付方式不能为空");
        }
    }

    /**
     * 生成唯一订单编号（格式：yyyyMMdd + 8位随机数）
     */
    private String generateOrderNo() {
        StringBuilder randomSb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < ORDER_NO_RANDOM_LEN; i++) {
            randomSb.append(random.nextInt(10));
        }
        return ORDER_NO_PREFIX + randomSb.toString();
    }

    /**
     * 生成支付链接（模拟，实际对接第三方支付接口）
     */
    private String generatePayUrl(String orderNo, PayTypeEnum payType) {
        String baseUrl = "https://pay.community-shop.com/pay?";
        return baseUrl + "orderNo=" + orderNo + "&payType=" + payType.name()
                + "&timestamp=" + System.currentTimeMillis();
    }

    /**
     * 校验订单查看权限（买家/卖家/管理员可查看）
     */
    private void validateOrderPermission(Long operatorId, Long buyerId, Long sellerId) {
        User operator = userService.getById(operatorId);
        boolean isBuyer = Objects.equals(operatorId, buyerId);
        boolean isSeller = Objects.equals(operatorId, sellerId);
        boolean isAdmin = operator != null && UserRoleEnum.ADMIN.equals(operator.getRole());
        if (!isBuyer && !isSeller && !isAdmin) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权限查看此订单详情");
        }
    }

    /**
     * 发送订单通知给买家（调用MessageService）
     */
    private void sendBuyerOrderNotice(Long buyerId, Long orderId, String title, String content) {
        MessageSendDTO sendDTO = new MessageSendDTO();
        sendDTO.setReceiverId(buyerId);
        sendDTO.setTitle(title);
        sendDTO.setContent(content);
        sendDTO.setBusinessId(orderId);
        sendDTO.setType(MessageTypeEnum.ORDER);
//        messageService.sendMessage(sendDTO, 0L); // 0表示系统发送
    }

    /**
     * 发送订单通知给卖家（调用MessageService）
     */
    private void sendSellerOrderNotice(Long sellerId, Long orderId, String title, String content) {
        MessageSendDTO sendDTO = new MessageSendDTO();
        sendDTO.setReceiverId(sellerId);
        sendDTO.setTitle(title);
        sendDTO.setContent(content);
//        sendDTO.setOrderId(orderId);
        sendDTO.setType(MessageTypeEnum.ORDER);
//        messageService.sendMessage(sendDTO, 0L); // 0表示系统发送
    }

    /**
     * 校验订单状态流转合法性
     * 仅允许特定状态之间的转换（如待支付→已支付、待支付→已取消等）
     * @param currentStatus 当前订单状态
     * @param targetStatus 目标订单状态
     */
    private void validateOrderStatusTransition(OrderStatusEnum currentStatus, OrderStatusEnum targetStatus) {
        // 状态流转规则：待支付→已支付/已取消；已支付→已发货/已取消；已发货→已完成/已退货；已完成→无后续；已取消→无后续
        switch (currentStatus) {
            case PENDING_PAYMENT:
                // 待支付状态仅允许转为已支付或已取消
                if (!OrderStatusEnum.PAID.equals(targetStatus) && !OrderStatusEnum.CANCELLED.equals(targetStatus)) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
                }
                break;
            case PAID:
                // 已支付状态仅允许转为已发货或已取消（需特殊业务审批，此处简化处理）
                if (!OrderStatusEnum.SHIPPED.equals(targetStatus) && !OrderStatusEnum.CANCELLED.equals(targetStatus)) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
                }
                break;
            case SHIPPED:
                // 已发货状态仅允许转为已完成或已退货
                if (!OrderStatusEnum.COMPLETED.equals(targetStatus) && !OrderStatusEnum.RETURNED.equals(targetStatus)) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case RETURNED:
                // 终态状态不允许任何转换
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
            default:
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 校验订单操作人权限
     * 不同状态更新需对应权限（如买家取消待支付订单、卖家发货等）
     * @param order 订单信息
     * @param targetStatus 目标状态
     * @param operatorId 操作人ID
     */
    private void validateOrderOperatorPermission(Order order, OrderStatusEnum targetStatus, Long operatorId) {
        Long buyerId = order.getBuyerId();
        Long sellerId = order.getSellerId();

        switch (targetStatus) {
            case CANCELLED:
                // 待支付订单可由买家取消；已支付订单取消需双方确认（此处简化为仅买家可发起）
                if (!buyerId.equals(operatorId)) {
                    throw new BusinessException(ErrorCode.PERMISSION_DENIED);
                }
                break;
            case PAID:
                // 仅买家可支付订单
                if (!buyerId.equals(operatorId)) {
                    throw new BusinessException(ErrorCode.PERMISSION_DENIED);
                }
                break;
            case SHIPPED:
                // 仅卖家可操作发货
                if (!sellerId.equals(operatorId)) {
                    throw new BusinessException(ErrorCode.PERMISSION_DENIED);
                }
                break;
            case COMPLETED:
                // 仅买家可确认收货完成订单
                if (!buyerId.equals(operatorId)) {
                    throw new BusinessException(ErrorCode.PERMISSION_DENIED);
                }
                break;
            case RETURNED:
                // 退货需买家发起，此处简化为仅买家可操作
                if (!buyerId.equals(operatorId)) {
                    throw new BusinessException(ErrorCode.PERMISSION_DENIED);
                }
                break;
            default:
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
