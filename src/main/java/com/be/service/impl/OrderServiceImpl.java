package com.be.service.impl;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.OrderStatusLog;
import com.be.entity.Product;
import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.repository.ProductRepository;
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
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        if (order.getItems() != null) {
            order.getItems().size(); // Eagerly load items list
        }
        if (order.getStatusLogs() != null) {
            order.getStatusLogs().size(); // Eagerly load status logs list
        }
        return order;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        if (status == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(status);
        
        if (status == OrderStatus.DONE) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(status)
                .note("Cập nhật trạng thái bởi quản trị viên")
                .build();
        orderStatusLogRepository.save(statusLog);
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == OrderStatus.DONE || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .note(reason != null ? reason : "Đơn hàng bị hủy bởi quản trị viên")
                .build();
        orderStatusLogRepository.save(statusLog);
        
        return orderRepository.save(order);
    }

    private void restoreStock(Order order) {
        if (order.getItems() == null) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null || item.getQuantity() == null) {
                continue;
            }

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStockQuantity() > 0) {
                product.setIsActive(true);
            }
            productRepository.save(product);
        }
    }
}
