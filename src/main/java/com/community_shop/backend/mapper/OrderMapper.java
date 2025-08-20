package com.community_shop.backend.mapper;

import com.community_shop.backend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 创建订单（下单功能）
     * @param order 订单实体
     * @return 插入结果影响行数
     */
    @Insert("INSERT INTO order(product_id, buyer_id, seller_id, amount, status, create_time, pay_time) " +
            "VALUES (#{productID}, #{buyerID}, #{sellerID}, #{amount}, #{status}, #{createTime}, #{payTime})")
    int insert(Order order);

    /**
     * 查询订单详情（订单页）
     * @param orderId 订单ID
     * @return 订单实体
     */
    @Select("SELECT * FROM order WHERE order_id = #{orderId}")
    Order selectById(Long orderId);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     * @return 更新结果影响行数
     */
    @Update("UPDATE order SET status = #{status} WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 更新支付时间（支付成功后）
     * @param orderId 订单ID
     * @param payTime 支付时间
     * @return 更新结果影响行数
     */
    @Update("UPDATE order SET pay_time = #{payTime} WHERE order_id = #{orderId}")
    int updatePayTime(@Param("orderId") Long orderId, @Param("payTime") LocalDateTime payTime);

    /**
     * 查询买家的订单（按状态筛选）
     * @param buyerId 买家ID
     * @param status 订单状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 订单列表
     */
    @Select("<script>" +
            "SELECT * FROM order WHERE buyer_id = #{buyerId} " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Order> selectByBuyerId(@Param("buyerId") Long buyerId,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 查询卖家的订单（按状态筛选）
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 订单列表
     */
    @Select("<script>" +
            "SELECT * FROM order WHERE seller_id = #{sellerId} " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Order> selectBySellerId(@Param("sellerId") Long sellerId,
                                 @Param("status") String status,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);
}
