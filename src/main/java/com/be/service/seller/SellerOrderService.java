package com.be.service.seller;

import com.be.common.enums.OrderStatus;
import com.be.dto.response.seller.OrderListResponse;
import com.be.dto.response.seller.OrderDetailResponse;
import com.be.dto.response.seller.OrderActionResponse;
import org.springframework.data.domain.Page;

public interface SellerOrderService {
    Page<OrderListResponse> getListByPage(int page);
    OrderDetailResponse getDetails(Long id);
    Page<OrderListResponse> getListByStatus(OrderStatus status, int page);
    Page<OrderListResponse> getListByMonth(int year, int month, int page);
    OrderActionResponse confirmOrder(Long orderId);
    OrderActionResponse startDelivery(Long orderId);
    OrderActionResponse completeOrder(Long orderId);
    OrderActionResponse cancelOrder(Long orderId, String cancelReason);
}
