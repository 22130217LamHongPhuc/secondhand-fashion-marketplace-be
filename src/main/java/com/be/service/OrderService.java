package com.be.service;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<Order> getAllOrders(Pageable pageable);
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);
    Order getOrderById(Long id);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    Order cancelOrder(Long orderId, String reason);
}
