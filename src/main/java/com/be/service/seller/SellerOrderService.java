package com.be.service.seller;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;

import java.util.List;

public interface SellerOrderService {
    List<Order> getListByPage(Long lastId, int page, int size);
    Order getDetails(Long id);
    List<Order> getListByStatus(OrderStatus status, Long lastId, int page, int size );
    Order createOrder();
    Order updateOrder();
    void deleteOrder();
}
