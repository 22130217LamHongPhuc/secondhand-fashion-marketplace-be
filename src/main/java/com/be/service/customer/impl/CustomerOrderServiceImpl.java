package com.be.service.customer.impl;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryItemResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;
import com.be.entity.Order;
import com.be.entity.OrderStatusLog;
import com.be.repository.OrderRepository;
import com.be.repository.OrderStatusLogRepository;
import com.be.service.customer.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderHistoryPageResponse getOrderHistory(Long customerId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else {
            orderPage = orderRepository.findByCustomerId(customerId, pageable);
        }

        orderPage.getContent().forEach(order -> {
            if (order.getItems() != null) order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        });

        List<OrderHistoryItemResponse> items = orderPage.getContent().stream()
                .map(OrderHistoryItemResponse::fromEntity)
                .collect(Collectors.toList());

        return new OrderHistoryPageResponse(
                items,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        if (order.getReviews() != null) {
            order.getReviews().size();
        }

        return OrderDetailResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(Long customerId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));

        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                    "Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING. Trạng thái hiện tại: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        String cancelNote = reason != null ? reason : "Khách hàng hủy đơn";
        order.setCancelReason(cancelNote);
        Order saved = orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(saved)
                .status(OrderStatus.CANCELLED)
                .note(cancelNote)
                .changedBy(saved.getCustomer())
                .build();
        orderStatusLogRepository.save(statusLog);

        // Eagerly initialize for response
        if (saved.getItems() != null) {
            saved.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        if (saved.getReviews() != null) saved.getReviews().size();

        return OrderDetailResponse.fromEntity(saved);
    }
}
