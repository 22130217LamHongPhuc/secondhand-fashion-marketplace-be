package com.be.service.seller;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.entity.User;

import java.util.List;

public interface SellerOrderService {
    List<Order> getListByPage(Long lastId, int page);
    Order getDetails(Long id);
    List<Order> getListByStatus(OrderStatus status, Long lastId, int page);
    Order createOrder(User user);
    Order updateOrder(Long id);
    void deleteOrder(Long id);
    List<Order> searchByKeyword(String keyword, int page);
    Order confirmOrder(Long orderId);
    Order startDelivery(Long orderId);
    Order completeOrder(Long orderId);
    Order cancelOrder(Long orderId, String cancelReason);
}
