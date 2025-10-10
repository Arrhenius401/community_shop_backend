package com.community_shop.backend.convert;

import com.community_shop.backend.dto.order.OrderCreateDTO;
import com.community_shop.backend.dto.order.OrderDetailDTO;
import com.community_shop.backend.dto.order.OrderListItemDTO;
import com.community_shop.backend.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Order 模块对象转换器
 * 处理 Order 实体与 DTO 之间的映射
 */
@Mapper(componentModel = "spring")
public interface OrderConvert {

    // 单例实例（非 Spring 环境使用）
    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    /**
     * Order 实体 -> OrderDetailDTO（订单详情响应）
     * 映射说明：直接匹配同名字段，枚举类型自动映射
     */
    OrderDetailDTO orderToOrderDetailDTO(Order order);

    /**
     * Order 实体 -> OrderListItemDTO（订单列表项）
     * 映射说明：简化商品信息，仅保留列表页所需字段
     */
    @Mappings({
            @Mapping(target = "productSummary", ignore = true), // 需关联 Product 实体拼接（如"商品1等3件"）
            @Mapping(target = "productImage", ignore = true) // 需关联 Product 实体获取首图
    })
    OrderListItemDTO orderToOrderListItemDTO(Order order);

    /**
     * OrderCreateDTO（订单创建请求）-> Order 实体
     * 映射说明：
     * 1. 初始化订单状态为待支付，创建时间由系统生成
     * 2. 卖家 ID 需关联 Product 实体查询后补充
     */
    @Mappings({
            @Mapping(target = "orderId", ignore = true),
            @Mapping(target = "sellerId", ignore = true), // 需通过 productId 查询商品获取卖家
            @Mapping(target = "receiverName", ignore = true), // 需关联 User 实体获取用户名
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "payTime", ignore = true),
            @Mapping(target = "shipTime", ignore = true),
            @Mapping(target = "receiveTime", ignore = true),
            @Mapping(target = "status", expression = "java(com.community_shop.backend.enums.CodeEnum.OrderStatusEnum.PENDING_PAYMENT)"),
            @Mapping(target = "buyerRemark", defaultValue = "") // 默认空备注
    })
    Order orderCreateDtoToOrder(OrderCreateDTO dto);

    /**
     * 批量转换 Order 列表 -> OrderListItemDTO 列表
     */
    List<OrderListItemDTO> orderListToOrderListItemList(List<Order> orders);

    /**
     * 批量转换 Order 列表 -> OrderDetailDTO 列表
     */
    List<OrderDetailDTO> orderListToOrderDetailList(List<Order> orders);


}
