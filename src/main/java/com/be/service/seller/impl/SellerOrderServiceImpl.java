package com.be.service.seller.impl;

import com.be.common.enums.OrderStatus;
import com.be.entity.Order;
import com.be.service.seller.SellerOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerOrderServiceImpl implements SellerOrderService {
    @Override
    public List<Order> getListByPage(Long lastId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order getDetails(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Order> getListByStatus(OrderStatus status, Long lastId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order createOrder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Order updateOrder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteOrder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
