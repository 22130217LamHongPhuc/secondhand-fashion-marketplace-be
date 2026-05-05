package com.be.repository;

import com.be.entity.Order;
import com.be.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopId(String shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatus(String shopId, OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);
}

