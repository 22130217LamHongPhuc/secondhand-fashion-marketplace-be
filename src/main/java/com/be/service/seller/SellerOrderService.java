package com.be.service.seller;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SellerOrderService {
    Page<OrderListResponse> searchOrders(OrderStatus status, String orderCode, LocalDateTime fromDate, LocalDateTime toDate, BigDecimal minPrice, BigDecimal maxPrice, int page);
    OrderDetailResponse getDetails(Long id);
    OrderActionResponse confirmOrder(Long orderId);
    OrderActionResponse startDelivery(Long orderId);
    OrderActionResponse completeOrder(Long orderId);
    OrderActionResponse cancelOrder(Long orderId, String cancelReason);
}
