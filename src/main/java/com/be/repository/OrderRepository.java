package com.be.repository;

import com.be.entity.Order;
import com.be.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "SELECT * FROM orders o WHERE o.id > :lastId ORDER BY o.id ASC", nativeQuery = true)
    Page<Order> getListByPage(@Param("lastId") Long lastId, Pageable pageable);

    @Query(value = "SELECT * FROM orders o WHERE o.id > :lastId AND o.status = :status ORDER BY o.id ASC", nativeQuery = true)
    Page<Order> getListByStatus(
            @Param("status") String status,
            @Param("lastId") Long lastId,
            Pageable pageable
    );

    @Query(value = "SELECT * FROM orders o WHERE o.created_at >= :startAt AND o.created_at < :endAt ORDER BY o.id ASC", nativeQuery = true)
    Page<Order> getListByMonth(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopId(Long shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);
}

