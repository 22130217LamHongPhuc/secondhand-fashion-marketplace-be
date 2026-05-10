package com.be.service.seller;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import org.springframework.data.domain.Page;

public interface SellerOrderService {
    Page<Order> getListByPage(Long lastId, int page);
    Order getDetails(Long id);
    Page<Order> getListByStatus(OrderStatus status, Long lastId, int page);
    Page<Order> getListByMonth(int year, int month, int page);
    Order confirmOrder(Long orderId);
    Order startDelivery(Long orderId);
    Order completeOrder(Long orderId);
    Order cancelOrder(Long orderId, String cancelReason);
}
