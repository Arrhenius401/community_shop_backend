package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.Order;

import java.util.List;

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
}
