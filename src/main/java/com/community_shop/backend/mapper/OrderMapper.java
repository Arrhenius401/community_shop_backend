package com.community_shop.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface OrderMapper extends BaseMapper<Order> {


    /**
     * XML文件可帮助处理复杂动态SQL场景，实现SQL与代码分离和满足高级映射需求（即多表查询）三方面
     * 因而辅助mapper功能的XML文件是必要的
     */

    /**
     * order 是 SQL 关键字（用于排序的 ORDER BY），作为表名时必须用反引号 ` 包裹
     * 否则数据库会将其解析为关键字而非表名，导致逻辑错误
     * 在 MyBatis 的 @Select 注解中，>=、<= 等比较符号可以直接书写，无需转义为 &gt;=、&lt;=
     * （转义通常用于 XML 配置文件，注解中冗余且降低可读性）。
     */

    /**
     * 转移符号
     * <    小于      &lt;    避免被解析为 XML 标签的开始（greater than）
     * >    大于      &gt;    避免被解析为 XML 标签的结束（less than）
     * &    逻辑与    &amp;    XML 中 & 是实体引用的起始符号，必须转义（ampersand，连字符 / 与符号）
     * '    单引号    &apos;   在属性值或字符串中使用时可能需要转义（apostrophe,撇号 / 单引号）
     * "    双引号    &quot;   当属性值用双引号包裹时，内部双引号需转义
     */


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
     * 按订单号查询订单（用于查询订单详情）
     * @param orderNo 订单号
     * @return 订单完整实体
     */
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询已过期且未支付的订单（支付超时订单）
     * @param payExpireTime 支付过期时间（用于筛选创建时间早于该时间的订单）
     * @return 超时未支付订单列表
     */
    @Select("SELECT * FROM `order` WHERE status = 'PENDING_PAYMENT' AND pay_expire_time <= #{payExpireTime} ORDER BY pay_expire_time ASC")
    List<Order> selectTimeoutPendingOrders(@Param("payExpireTime") LocalDateTime payExpireTime);

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
