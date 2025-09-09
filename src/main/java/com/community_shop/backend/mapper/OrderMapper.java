package com.community_shop.backend.mapper;

import com.community_shop.backend.enums.CodeEnum.OrderStatusEnum;
import com.community_shop.backend.entity.Order;
import com.community_shop.backend.enums.SimpleEnum.PayTypeEnum;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单管理模块Mapper接口，严格对应order表结构（文档4_数据库设计.docx）
 */
@Mapper
public interface OrderMapper {

    // ==================== 基础CRUD ====================
    /**
     * 创建订单（插入订单数据）
     * @param order 订单实体（含商品ID、买卖双方ID、金额等核心字段）
     * @return 影响行数
     */
    int insert(Order order);

    /**
     * 通过订单ID查询订单详情
     * @param orderId 订单唯一标识
     * @return 订单完整实体
     */
    Order selectById(@Param("orderId") Long orderId);

    /**
     * 删除订单（仅限未支付等特定状态）
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteById(@Param("orderId") Long orderId);


    // ==================== 状态与时间更新 ====================
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 目标状态（枚举）
     * @return 影响行数
     */
    int updateStatus(@Param("orderId") Long orderId, @Param("status") OrderStatusEnum status);

    /**
     * 更新支付时间
     * @param orderId 订单ID
     * @param payTime 支付时间
     * @param payType 支付方式（枚举）
     * @return 影响行数
     */
    int updatePayInfo(
            @Param("orderId") Long orderId,
            @Param("payTime") LocalDateTime payTime,
            @Param("payType") PayTypeEnum payType
    );

    /**
     * 更新发货时间
     * @param orderId 订单ID
     * @param shipTime 发货时间
     * @return 影响行数
     */
    int updateShipTime(@Param("orderId") Long orderId, @Param("shipTime") LocalDateTime shipTime);

    /**
     * 更新收货时间
     * @param orderId 订单ID
     * @param receiveTime 收货时间
     * @return 影响行数
     */
    int updateReceiveTime(@Param("orderId") Long orderId, @Param("receiveTime") LocalDateTime receiveTime);

    /**
     * 更新支付时间（支付成功后）
     * @param orderId 订单ID
     * @param payTime 支付时间
     * @return 更新结果影响行数
     */
    @Update("UPDATE `order` SET pay_time = #{payTime} WHERE order_id = #{orderId}")
    int updatePayTime(@Param("orderId") Long orderId, @Param("payTime") LocalDateTime payTime);


    // ==================== 条件查询 ====================
    /**
     * 分页查询买家订单（支持按状态筛选）
     * @param buyerId 买家ID
     * @param status 订单状态（枚举，可为null）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 买家订单分页列表
     */
    List<Order> selectByBuyerId(
            @Param("buyerId") Long buyerId,
            @Param("status") OrderStatusEnum status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 分页查询卖家订单（支持按状态筛选）
     * @param sellerId 卖家ID
     * @param status 订单状态（枚举，可为null）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 卖家订单分页列表
     */
    List<Order> selectBySellerId(
            @Param("sellerId") Long sellerId,
            @Param("status") OrderStatusEnum status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计买家指定状态的订单数量
     * @param buyerId 买家ID
     * @param status 订单状态（枚举）
     * @return 订单数量
     */
    int countByBuyerId(@Param("buyerId") Long buyerId, @Param("status") OrderStatusEnum status);

    /**
     * 统计卖家指定状态的订单数量
     * @param sellerId 卖家ID
     * @param status 订单状态（枚举）
     * @return 订单数量
     */
    int countBySellerId(@Param("sellerId") Long sellerId, @Param("status") OrderStatusEnum status);

    /**
     * 按时间范围查询订单（区分买家/卖家角色）
     * @param userId 用户ID（买家或卖家）
     * @param role 角色（"BUYER"或"SELLER"）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 订单分页列表
     */
    List<Order> selectByCreateTimeRange(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
