package com.be.service.impl;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.repository.OrderRepository;
import com.be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        
        return orderRepository.save(order);
    }
}
