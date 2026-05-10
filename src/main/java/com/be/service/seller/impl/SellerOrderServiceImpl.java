package com.be.service.seller.impl;

import com.be.common.enums.OrderStatus;
import com.be.constant.Constant;
import com.be.entity.Order;
import com.be.entity.OrderStatusLog;
import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.service.seller.SellerOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

    @Override
    public Page<Order> getListByPage(Long lastId, int page) {
        long cursor = lastId == null ? 0L : lastId;
        return orderRepository.getListByPage(cursor, PageRequest.of(page, Constant.ORDER_SIZE));
    }

    @Override
    public Order getDetails(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Page<Order> getListByStatus(OrderStatus status, Long lastId, int page) {
        long cursor = lastId == null ? 0L : lastId;
        return orderRepository.getListByStatus(status.name(), cursor, PageRequest.of(page, Constant.ORDER_SIZE));
    }

    @Override
    public Page<Order> getListByMonth(int year, int month, int page) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return orderRepository.getListByMonth(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(),
                PageRequest.of(page, Constant.ORDER_SIZE)
        );
    }

    @Override
    @Transactional
    public Order confirmOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public Order startDelivery(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.SHIPPING);
    }

    @Override
    @Transactional
    public Order completeOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.DONE);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        Order order = updateOrderStatus(orderId, OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        return orderRepository.save(order);
    }

    private Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.CONFIRMED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPING;
            case SHIPPING -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.DONE;
            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Unsuitable status");
        }
    }
}
