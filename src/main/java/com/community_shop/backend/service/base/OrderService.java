package com.community_shop.backend.service.base;

import com.community_shop.backend.dto.PageParam;
import com.community_shop.backend.dto.PageResult;
import com.community_shop.backend.dto.order.*;
import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易流程Service接口，实现《文档》中订单创建、支付、售后等核心功能
 * 依据：
 * 1. 《文档1_需求分析.docx》：订单创建、支付、物流、售后
 * 2. 《文档4_数据库工作（新）.docx》：order表结构（order_id、buyer_id、seller_id、status等）
 * 3. 《代码文档1 Mapper层设计.docx》：OrderMapper的CRUD及状态更新方法
 */
@Service
public interface OrderService extends BaseService<Order> {

    /**
     * 创建订单（支持单商品/多商品合并）
     * @param userId 买家ID
     * @param orderCreateDTO 订单创建参数
     * @return 未支付订单详情（含支付链接/二维码）
     * @throws BusinessException 商品库存不足、买家信用分过低等场景抛出
     */
    OrderDetailDTO createOrder(Long userId, OrderCreateDTO orderCreateDTO);

    /**
     * 取消订单
     * @param userId 操作人ID（买家/管理员）
     * @param orderId 订单ID
     * @return 是否取消成功
     * @throws BusinessException 订单状态不允许取消（已支付/已发货）、无权限等场景抛出
     */
    Boolean cancelOrder(Long userId, Long orderId);

    /**
     * 支付订单回调处理
     * @param payCallbackDTO 支付平台回调参数
     * @return 回调处理结果（用于支付平台确认）
     * @throws BusinessException 订单不存在、签名验证失败等场景抛出
     */
    String handlePayCallback(PayCallbackDTO payCallbackDTO);

    /**
     * 卖家发货
     * @param sellerId 卖家ID
     * @param orderId 订单ID
     * @param shipDTO 发货参数（物流单号、快递公司）
     * @return 发货后的订单详情
     * @throws BusinessException 无权限、订单未支付等场景抛出
     */
    OrderDetailDTO shipOrder(Long sellerId, Long orderId, OrderShipDTO shipDTO);

    /**
     * 买家确认收货
     * @param buyerId 买家ID
     * @param orderId 订单ID
     * @return 确认后的订单详情
     * @throws BusinessException 无权限、订单未发货等场景抛出
     */
    OrderDetailDTO confirmReceive(Long buyerId, Long orderId);

    /**
     * 查询订单详情
     * @param userId 操作人ID（买家/卖家/管理员）
     * @param orderId 订单ID
     * @return 订单详情
     * @throws BusinessException 无权限、订单不存在等场景抛出
     */
    OrderDetailDTO getOrderDetail(Long userId, Long orderId);

    /**
     * 买家查询订单列表
     * @param buyerId 买家ID
     * @param queryDTO 订单查询参数（含状态筛选、分页）
     * @return 分页订单列表
     */
    PageResult<OrderListItemDTO> getBuyerOrders(Long buyerId, OrderQueryDTO queryDTO);

    /**
     * 卖家查询订单列表
     * @param sellerId 卖家ID
     * @param queryDTO 订单查询参数（含状态筛选、分页）
     * @return 分页订单列表
     */
    PageResult<OrderListItemDTO> getSellerOrders(Long sellerId, OrderQueryDTO queryDTO);

    /**
     * 自动关闭超时未支付订单（定时任务调用）
     * @param timeoutMinutes 超时时间（分钟）
     * @return 关闭成功的订单数量
     */
    int autoCloseTimeoutOrders(int timeoutMinutes);

    /**
     * 更新订单状态（基础CRUD）
     * 核心逻辑：按状态校验操作权限（如"待支付"仅买家可取消），调用OrderMapper.updateStatus更新
     * @param orderId 订单ID
     * @param status 目标状态（需符合状态流转规则）
     * @param operatorId 操作用户ID（买家/卖家/管理员）
     * @return 成功返回true，失败抛出异常或返回false
     * @see com.community_shop.backend.mapper.OrderMapper#updateStatus(Long, OrderStatusEnum)
     * @see 《文档2_系统设计.docx》订单状态流转规则
     */
    Boolean updateOrderStatus(Long orderId, OrderStatusEnum status, Long operatorId);

}