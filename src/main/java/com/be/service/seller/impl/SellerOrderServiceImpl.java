package com.be.service.seller.impl;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.entity.OrderStatusLog;
import com.be.entity.User;
import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

    @Override
    public List<Order> getListByPage(Long lastId, int page) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order getDetails(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Order> getListByStatus(OrderStatus status, Long lastId, int page) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order createOrder(@AuthenticationPrincipal User user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order updateOrder(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteOrder(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Order> searchByKeyword(String keyword, int page) {
        return List.of();
    }

    @Override
    @Transactional
    public Order confirmOrder(Long orderId) {
        return changeStatus(orderId, OrderStatus.CONFIRMED, "Order confirmed");
    }

    @Override
    @Transactional
    public Order startDelivery(Long orderId) {
        return changeStatus(orderId, OrderStatus.SHIPPING, "Order started delivery");
    }

    @Override
    @Transactional
    public Order completeOrder(Long orderId) {
        return changeStatus(orderId, OrderStatus.DONE, "Order completed");
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        Order order = changeStatus(orderId, OrderStatus.CANCELLED, cancelReason);
        order.setCancelReason(cancelReason);
        return orderRepository.save(order);
    }

    private Order changeStatus(Long orderId, OrderStatus newStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        orderStatusLogRepository.save(OrderStatusLog.builder()
                .order(savedOrder)
                .status(newStatus)
                .note(note)
                .build());

        return savedOrder;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean valid = switch (newStatus) {
            case CONFIRMED -> currentStatus == OrderStatus.PENDING;
            case SHIPPING -> currentStatus == OrderStatus.CONFIRMED;
            case DONE -> currentStatus == OrderStatus.SHIPPING;
            case CANCELLED -> currentStatus == OrderStatus.PENDING || currentStatus == OrderStatus.CONFIRMED;
            case PENDING -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Cannot change order status from " + currentStatus + " to " + newStatus);
        }
    }
}
