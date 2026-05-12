package com.be.repository;

import com.be.entity.Order;
import com.be.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByShopId(Long shopId);
    Optional<Order> findByOrderCode(String orderCode);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DONE'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'DONE'")
    BigDecimal calculateShopRevenue(@Param("shopId") Long shopId);
}
