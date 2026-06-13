package com.be.repository;

import com.be.entity.Order;
import com.be.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> getListByPage(Pageable pageable);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Page<Order> getListByStatus(
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.createdAt >= :startAt AND o.createdAt < :endAt ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startAt AND o.createdAt < :endAt")
    Page<Order> getListByMonth(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"customer", "items", "shippingAddress"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopId(Long shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

        @Query("SELECT COALESCE(SUM(o.subtotal), 0) FROM Order o")
        BigDecimal sumTotalRevenue();
    @Query("""
            SELECT o.shop.id
            FROM Order o
            WHERE o.createdAt >= :startAt
              AND o.status <> :excludedStatus
            GROUP BY o.shop.id
            ORDER BY COUNT(o.id) DESC
            """)
    List<Long> findTopShopIdsByOrderCountSince(
            @Param("startAt") LocalDateTime startAt,
            @Param("excludedStatus") OrderStatus excludedStatus,
            Pageable pageable
    );


}

