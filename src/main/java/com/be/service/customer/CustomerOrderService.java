package com.be.service.customer;

import com.be.common.enums.OrderStatus;
import com.be.dto.request.customer.CheckoutRequest;
import com.be.dto.response.customer.CheckoutResponse;
import com.be.dto.response.customer.OrderDetailResponse;
import com.be.dto.response.customer.OrderHistoryPageResponse;
import java.util.List;

public interface CustomerOrderService {

    OrderHistoryPageResponse getOrderHistory(Long customerId, OrderStatus status, int page, int size);

    OrderDetailResponse getOrderDetail(Long customerId, Long orderId);

    OrderDetailResponse cancelOrder(Long customerId, Long orderId, String reason);

    CheckoutResponse checkout(CheckoutRequest request);
}
