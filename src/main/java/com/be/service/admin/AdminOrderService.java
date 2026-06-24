package com.be.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {
    
    /**
     * Get all orders with pagination and optional status filter
     * Admin can view all orders from all shops
     */
    Page<?> getAllOrders(Pageable pageable, String status);
    
    /**
     * Get order details by ID
     */
    Object getOrderById(Long id);
    
    /**
     * Get order statistics
     */
    Object getOrderStatistics();
}
