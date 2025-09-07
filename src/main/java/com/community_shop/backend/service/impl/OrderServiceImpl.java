package com.community_shop.backend.service.impl;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.vo.order.OrderCreateVO;
import com.community_shop.backend.enums.codeEnum.OrderStatusEnum;
import com.community_shop.backend.enums.errorcode.ErrorCode;
import com.community_shop.backend.exception.BusinessException;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.entity.Product;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.OrderMapper;
import com.community_shop.backend.service.base.MessageService;
import com.community_shop.backend.service.base.OrderService;
import com.community_shop.backend.service.base.ProductService;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 订单服务实现类，实现订单创建、支付、查询等核心业务逻辑
 * 依赖UserService校验用户信用、ProductService处理库存、MessageService发送通知
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    // 缓存相关常量
    private static final String CACHE_KEY_ORDER = "order:info:"; // 订单信息缓存Key前缀
    private static final String CACHE_KEY_USER_ORDERS = "order:user:"; // 用户订单列表缓存Key前缀
    private static final long CACHE_TTL_ORDER = 30; // 订单缓存有效期（分钟）

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
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增订单（基础CRUD）
     * 核心逻辑：初始化订单状态为"待支付"，记录创建时间，插入数据库
     */
    @Override
    public Long insertOrder(Order order) {
        try {
            // 1. 参数校验：必填字段非空校验
            if (order.getProductId() == null || order.getBuyerId() == null ||
                    order.getSellerId() == null || order.getTotalAmount() == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 初始化订单基础信息
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT); // 初始状态：待支付
            order.setCreateTime(LocalDateTime.now()); // 记录下单时间
            order.setPayTime(null); // 未支付时支付时间为空

            // 3. 插入数据库
            int insertRows = orderMapper.insert(order);
            if (insertRows <= 0) {
                log.error("新增订单失败，订单信息：{}", order);
                throw new BusinessException(ErrorCode.DATA_INSERT_FAILED);
            }

            // 4. 缓存订单信息
            redisTemplate.opsForValue().set(CACHE_KEY_ORDER + order.getOrderId(), order, CACHE_TTL_ORDER * 60);
            log.info("新增订单成功，订单ID：{}", order.getOrderId());
            return order.getOrderId();

        } catch (BusinessException e) {
            throw e; // 抛出业务异常，由全局处理器处理
        } catch (Exception e) {
            log.error("新增订单系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 按订单ID查询（基础CRUD）
     * 核心逻辑：校验操作用户权限，关联商品信息后返回
     */
    @Override
    public Order selectOrderById(Long orderId, Long userId) {
        // 1. 参数校验
        if (orderId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }

        // 2. 先查缓存
        Order order = (Order) redisTemplate.opsForValue().get(CACHE_KEY_ORDER + orderId);
        if (Objects.nonNull(order)) {
            // 3. 权限校验：仅买家或卖家可查看
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }
            // 4. 关联商品信息
            Product product = productService.selectProductById(order.getProductId());
            order.setProductId(product.getProductId());
            return order;
        }

        // 5. 缓存未命中，查数据库
        order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，订单ID：{}", orderId);
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }

        // 6. 权限校验（数据库查询结果二次校验）
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 7. 关联商品信息并缓存
        Product product = productService.selectProductById(order.getProductId());
        order.setProductId(product.getProductId());
        redisTemplate.opsForValue().set(CACHE_KEY_ORDER + orderId, order, CACHE_TTL_ORDER * 60);
        log.info("查询订单成功，订单ID：{}，操作用户ID：{}", orderId, userId);
        return order;
    }

    /**
     * 按买家查询订单列表（基础CRUD，分页）
     */
    @Override
    public PageResult<Order> selectOrderByBuyer(Long buyerId, OrderStatusEnum status, PageParam pageParam) {
        try {
            // 1. 参数校验
            if (buyerId == null || pageParam == null || pageParam.getPageNum() < 1 || pageParam.getPageSize() < 1) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验用户存在
            User buyer = userService.selectUserById(buyerId);
            if (buyer == null) {
                throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
            }

            // 3. 计算分页参数（offset = (页码-1)*页大小）
            int offset = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int limit = pageParam.getPageSize();

            // 4. 查询订单列表
            List<Order> orderList = orderMapper.selectByBuyerId(buyerId, status, offset, limit);

            // 5. 查询总条数（用于分页计算）
            int total = orderMapper.countByBuyerId(buyerId, status);

            // 7. 关联商品信息
            for (Order order : orderList) {
                Product product = productService.selectProductById(order.getProductId());
                order.setProductId(product.getProductId());
            }

            // 8. 构建分页结果
            PageResult<Order> pageResult = new PageResult<>();
            pageResult.setList(orderList);
            pageResult.setTotal(total);
            pageResult.setPageNum(pageParam.getPageNum());
            pageResult.setPageSize(pageParam.getPageSize());
            pageResult.setTotalPages((total + limit - 1) / limit); // 计算总页数

            log.info("查询买家订单列表成功，买家ID：{}，状态：{}，页码：{}", buyerId, status, pageParam.getPageNum());
            return pageResult;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询买家订单列表系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
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

    /**
     * 按订单ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：仅允许买家删除"已取消"或"已完成"订单
     */
    @Override
    public Boolean deleteOrderById(Long orderId, Long buyerId) {
        try {
            // 1. 参数校验
            if (orderId == null || buyerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 查询订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }

            // 3. 校验买家身份（仅订单所属买家可删除）
            if (!order.getBuyerId().equals(buyerId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 校验订单状态（仅已取消/已完成订单可删除）
            if (!OrderStatusEnum.CANCELLED.equals(order.getStatus()) &&
                    !OrderStatusEnum.COMPLETED.equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            }

            // 5. 逻辑删除订单（假设deleteById为逻辑删除，更新is_deleted字段）
            int deleteRows = orderMapper.deleteById(orderId);
            if (deleteRows <= 0) {
                log.error("删除订单失败，订单ID：{}，买家ID：{}", orderId, buyerId);
                throw new BusinessException(ErrorCode.DATA_DELETE_FAILED);
            }

            // 6. 清理缓存
            redisTemplate.delete(CACHE_KEY_ORDER + orderId);
            redisTemplate.delete(CACHE_KEY_USER_ORDERS + buyerId);

            log.info("删除订单成功，订单ID：{}，买家ID：{}", orderId, buyerId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除订单系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 创建订单（业务方法，事务控制）
     * 核心逻辑：校验用户信用与商品库存，创建订单并扣减库存，保证数据一致性
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Order createOrder(OrderCreateVO orderCreateVO, Long buyerId) {
        try {
            // 1. 参数校验
            if (orderCreateVO == null || orderCreateVO.getProductId() == null ||
                    !StringUtils.hasText(orderCreateVO.getAddress()) || buyerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 校验买家信用分（需≥60分才能下单）
            User buyer = userService.selectUserById(buyerId);
            if (buyer.getCreditScore() < MIN_CREDIT_FOR_ORDER) {
                throw new BusinessException(ErrorCode.CREDIT_TOO_LOW);
            }

            // 3. 校验商品信息与库存
            Product product = productService.selectProductById(orderCreateVO.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXISTS);
            }
            if (product.getStock() < 1) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }

            // 4. 构建订单实体
            Order order = new Order(orderCreateVO);
            order.setSellerId(product.getSellerId());

            // 5. 调用基础方法插入订单
            insertOrder(order);
            Long orderId = order.getOrderId();

            // 6. 扣减商品库存（事务内操作，失败则回滚订单创建）
            productService.updateStock(product.getProductId(), -1, "下单扣减库存");

            log.info("创建订单成功，订单ID：{}，商品ID：{}，买家ID：{}",
                    orderId, product.getProductId(), buyerId);
            return order;

        } catch (BusinessException e) {
            throw e; // 事务会自动回滚
        } catch (Exception e) {
            log.error("创建订单系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR); // 事务回滚
        }
    }

    /**
     * 订单支付（业务方法）
     * 核心逻辑：校验订单状态，更新支付信息，发送通知给卖家
     */
    @Override
    public String payOrder(Long orderId, String payNo, LocalDateTime payTime, Long buyerId) {
        try {
            // 1. 参数校验
            if (orderId == null || !StringUtils.hasText(payNo) || payTime == null || buyerId == null) {
                throw new BusinessException(ErrorCode.PARAM_NULL);
            }

            // 2. 查询订单并校验状态（仅"待支付"订单可支付）
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
            }
            if (!OrderStatusEnum.PENDING_PAYMENT.equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            }

            // 3. 校验买家身份（仅订单所属买家可支付）
            if (!order.getBuyerId().equals(buyerId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            }

            // 4. 更新订单支付状态与支付信息
            int updateRows = orderMapper.updatePayTime(orderId, payTime);
            if (updateRows <= 0) {
                log.error("更新订单支付时间失败，订单ID：{}", orderId);
                throw new BusinessException(ErrorCode.DATA_UPDATE_FAILED);
            }
            // 更新订单状态为"已支付"
            updateOrderStatus(orderId, OrderStatusEnum.PAID, buyerId);

            // 5. 发送站内信通知卖家（订单已支付，提醒发货）
            String noticeContent = String.format("订单%s已支付，请尽快发货", orderId);
            messageService.sendSellerNotice(order.getSellerId(), noticeContent, orderId);

            log.info("订单支付成功，订单ID：{}，支付单号：{}，买家ID：{}",
                    orderId, payNo, buyerId);
            return "支付成功";

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("订单支付系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
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
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
                }
                break;
            case PAID:
                // 已支付状态仅允许转为已发货或已取消（需特殊业务审批，此处简化处理）
                if (!OrderStatusEnum.SHIPPED.equals(targetStatus) && !OrderStatusEnum.CANCELLED.equals(targetStatus)) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
                }
                break;
            case SHIPPED:
                // 已发货状态仅允许转为已完成或已退货
                if (!OrderStatusEnum.COMPLETED.equals(targetStatus) && !OrderStatusEnum.RETURNED.equals(targetStatus)) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case RETURNED:
                // 终态状态不允许任何转换
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            default:
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
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
