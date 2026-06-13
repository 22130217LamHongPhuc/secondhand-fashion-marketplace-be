package com.be.service.customer;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;

public interface CustomerOrderService {

    OrderHistoryPageResponse getOrderHistory(Long customerId, OrderStatus status, int page, int size);

    OrderDetailResponse getOrderDetail(Long customerId, Long orderId);

    OrderDetailResponse cancelOrder(Long customerId, Long orderId, String reason);
}
