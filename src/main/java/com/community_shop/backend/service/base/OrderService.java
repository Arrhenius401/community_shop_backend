package com.community_shop.backend.service.base;

import com.community_shop.backend.DTO.param.PageParam;
import com.community_shop.backend.DTO.result.PageResult;
import com.community_shop.backend.DTO.result.ResultDTO;
import com.community_shop.backend.VO.OrderCreateVO;
import com.community_shop.backend.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易流程Service接口，实现《文档》中订单创建、支付、售后等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：订单创建、支付、物流、售后
 * 2. 《文档4_数据库工作（新）.docx》：order表结构（order_id、buyer_id、seller_id、status等）
 * 3. 《代码文档1 Mapper层设计.docx》：OrderMapper的CRUD及状态更新方法
 */
public interface OrderService {
    // 获取所有订单
    List<Order> getAllOrders();

    // 获取订单详情
    Order getOrderById(int id);

    // 添加订单
    int addOrder(Order order);

    // 更新订单信息
    int updateOrder(Order order);

    // 删除订单
    int deleteOrder(int id);

    /**
     * 新增订单（基础CRUD）
     * 核心逻辑：初始化订单状态为"待支付"，记录创建时间，调用OrderMapper.insert插入
     * @param order 订单实体（含productId、buyerId、sellerId、amount，不含order_id）
     * @return ResultDTO<Long> 成功返回新增订单ID，失败返回错误信息
     * @see com.community_shop.backend.mapper.OrderMapper#insert(Order)
     */
    ResultDTO<Long> insertOrder(Order order);

    /**
     * 按订单ID查询（基础CRUD）
     * 核心逻辑：调用OrderMapper.selectById查询，校验仅买卖双方可查，关联商品信息
     * @param orderId 订单ID（主键）
     * @param userId 操作用户ID（买家或卖家）
     * @return ResultDTO<Order> 成功返回含商品信息的订单详情，失败返回错误信息（如"无权限查看"）
     * @see com.community_shop.backend.mapper.OrderMapper#selectById(Long)
     * @see ProductService#selectProductById(Long)
     */
    ResultDTO<Order> selectOrderById(Long orderId, Long userId);

    /**
     * 按买家查询订单列表（基础CRUD，分页）
     * 核心逻辑：调用OrderMapper.selectByBuyerId查询，按状态筛选（如"待支付"/"已发货"）
     * @param buyerId 买家ID
     * @param status 订单状态（枚举值："PENDING_PAY"/"PAID"/"SHIPPED"/"COMPLETED"/"CANCELLED"）
     * @param pageParam 分页参数（页码、每页条数）
     * @return ResultDTO<PageResult<Order>> 成功返回分页订单列表，失败返回错误信息
     * @see com.community_shop.backend.mapper.OrderMapper#selectByBuyerId(Long, String, int, int)
     * @see com.community_shop.backend.component.enums.OrderStatusEnum （订单状态枚举）
     */
    ResultDTO<PageResult<Order>> selectOrderByBuyer(Long buyerId, String status, PageParam pageParam);

    /**
     * 更新订单状态（基础CRUD）
     * 核心逻辑：按状态校验操作权限（如"待支付"仅买家可取消），调用OrderMapper.updateStatus更新
     * @param orderId 订单ID
     * @param status 目标状态（需符合状态流转规则）
     * @param operatorId 操作用户ID（买家/卖家/管理员）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息（如"状态非法"）
     * @see com.community_shop.backend.mapper.OrderMapper#updateStatus(Long, String)
     * @see 《文档2_系统设计.docx》订单状态流转规则
     */
    ResultDTO<Boolean> updateOrderStatus(Long orderId, String status, Long operatorId);

    /**
     * 按订单ID删除（基础CRUD，逻辑删除）
     * 核心逻辑：校验仅买家可操作，且订单状态为"已取消"/"已完成"，调用OrderMapper.deleteById标记删除
     * @param orderId 待删除订单ID
     * @param buyerId 买家ID（需与订单buyer_id一致）
     * @return ResultDTO<Boolean> 成功返回true，失败返回错误信息
     * @see com.community_shop.backend.mapper.OrderMapper#deleteById(Long)
     */
    ResultDTO<Boolean> deleteOrderById(Long orderId, Long buyerId);

    /**
     * 创建订单（业务方法，事务控制）
     * 核心逻辑：事务隔离级别READ_COMMITTED，校验买家信用分≥60分、商品库存充足，创建订单并扣减库存
     * @param orderCreateVO 订单创建参数（商品ID、收货地址、支付方式）
     * @param buyerId 买家ID
     * @return ResultDTO<Order> 成功返回订单信息+支付链接，失败回滚事务并返回错误信息
     * @see #insertOrder(Order)
     * @see ProductService#updateStock(Long, Integer, String)
     * @see UserService#selectUserById(Long)
     * @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
     */
    ResultDTO<Order> createOrder(OrderCreateVO orderCreateVO, Long buyerId);

    /**
     * 订单支付（业务方法）
     * 核心逻辑：校验订单状态为"待支付"，更新状态为"已支付"并记录支付时间，发送站内信通知卖家
     * @param orderId 订单ID
     * @param payNo 支付单号（第三方支付平台返回）
     * @param payTime 支付时间
     * @param buyerId 买家ID（需与订单buyer_id一致）
     * @return ResultDTO<String> 成功返回"支付成功"，失败返回错误信息
     * @see com.community_shop.backend.mapper.OrderMapper#updatePayTime(Long, java.time.LocalDateTime)
     * @see com.community_shop.backend.service.base.MessageService#sendSellerNotice(Long, String, Long) （站内信服务）
     */
    ResultDTO<String> payOrder(Long orderId, String payNo, LocalDateTime payTime, Long buyerId);

//    /**
//     * 申请售后（业务方法，事务控制）
//     * 核心逻辑：事务隔离级别READ_COMMITTED，校验订单状态为"已发货"/"已完成"，创建售后记录并更新订单状态
//     * @param orderId 订单ID
//     * @param type 售后类型（"REFUND"=退货，"EXCHANGE"=换货）
//     * @param reason 售后原因（如"商品质量问题"）
//     * @param buyerId 买家ID（需与订单buyer_id一致）
//     * @return ResultDTO<String> 成功返回售后申请详情，失败回滚事务并返回错误信息
//     * @see com.community_shop.backend.mapper.OrderMapper#insertAfterSaleRecord(Long, String, String)
//     * @see #updateOrderStatus(Long, String, Long)
//     * @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
//     */
//    ResultDTO<String> applyAfterSale(Long orderId, String type, String reason, Long buyerId);
}
