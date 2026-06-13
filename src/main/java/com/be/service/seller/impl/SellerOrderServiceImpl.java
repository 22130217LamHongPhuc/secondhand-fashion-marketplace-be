package com.be.service.seller.impl;

import com.be.common.enums.OrderStatus;
import com.be.constant.Constant;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import com.be.dto.response.seller.mapper.SellerOrderMapper;
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
    public Page<OrderListResponse> getListByPage(int page) {
        Page<Order> orders = orderRepository.getListByPage(PageRequest.of(page, Constant.ORDER_SIZE));
        return orders.map(SellerOrderMapper::toListResponse);
    }

    @Override
    public OrderDetailResponse getDetails(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return SellerOrderMapper.toDetailResponse(order);
    }

    @Override
    public Page<OrderListResponse> getListByStatus(OrderStatus status, int page) {
        Page<Order> orders = orderRepository.getListByStatus(status, PageRequest.of(page, Constant.ORDER_SIZE));
        return orders.map(SellerOrderMapper::toListResponse);
    }

    @Override
    public Page<OrderListResponse> getListByMonth(int year, int month, int page) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Page<Order> orders = orderRepository.getListByMonth(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(),
                PageRequest.of(page, Constant.ORDER_SIZE)
        );
        return orders.map(SellerOrderMapper::toListResponse);
    }

    @Override
    @Transactional
    public OrderActionResponse confirmOrder(Long orderId) {
        Order order = updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        return SellerOrderMapper.toActionResponse(order);
    }

    @Override
    @Transactional
    public OrderActionResponse startDelivery(Long orderId) {
        Order order = updateOrderStatus(orderId, OrderStatus.SHIPPING);
        return SellerOrderMapper.toActionResponse(order);
    }

    @Override
    @Transactional
    public OrderActionResponse completeOrder(Long orderId) {
        Order order = updateOrderStatus(orderId, OrderStatus.DONE);
        return SellerOrderMapper.toActionResponse(order);
    }

    @Override
    @Transactional
    public OrderActionResponse cancelOrder(Long orderId, String cancelReason) {
        Order order = updateOrderStatus(orderId, OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        Order savedOrder = orderRepository.save(order);
        return SellerOrderMapper.toActionResponse(savedOrder);
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
