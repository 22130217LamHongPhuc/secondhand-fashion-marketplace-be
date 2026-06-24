package com.be.service.admin.impl;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.repository.OrderRepository;
import com.be.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {
    
    private final OrderRepository orderRepository;
    
    @Override
    public Page<?> getAllOrders(Pageable pageable, String status) {
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("status"), orderStatus),
                    pageable
                );
            } catch (IllegalArgumentException e) {
                // Invalid status, return all
            }
        }
        return orderRepository.findAll(pageable);
    }
    
    @Override
    public Object getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + id));
    }
    
    @Override
    public Object getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.PENDING)
        );
        long confirmedOrders = orderRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.CONFIRMED)
        );
        long shippingOrders = orderRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.SHIPPING)
        );
        long doneOrders = orderRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.DONE)
        );
        long cancelledOrders = orderRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.CANCELLED)
        );
        
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("confirmedOrders", confirmedOrders);
        stats.put("shippingOrders", shippingOrders);
        stats.put("doneOrders", doneOrders);
        stats.put("cancelledOrders", cancelledOrders);
        
        return stats;
    }
}
