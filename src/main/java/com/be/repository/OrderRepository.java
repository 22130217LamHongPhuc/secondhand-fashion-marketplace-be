package com.be.repository;

import com.be.entity.Order;
import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentStatus;
import com.be.common.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.QueryHint;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    long countByShopId(Long shopId);

    @QueryHints(value = @QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer WHERE o.shop.id = :shopId ORDER BY o.createdAt DESC")
    java.util.stream.Stream<Order> streamAllByShopIdOrderByCreatedAtDesc(@Param("shopId") Long shopId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.paymentStatus = :paymentStatus AND o.paymentMethod = :paymentMethod AND o.createdAt < :limit")
    List<Order> findExpiredOrders(
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("limit") LocalDateTime limit
    );

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> getListByPage(Pageable pageable);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status and o.orderCode = :orderCode ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Page<Order> getListByStatusAndOrderCode(
            @Param("status") OrderStatus status,
            @Param("orderCode") String orderCode,
            Pageable pageable
    );

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.createdAt >= :startAt AND o.createdAt < :endAt ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startAt AND o.createdAt < :endAt")
    Page<Order> getListByMonth(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.shop.id = :shopId ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId")
    Page<Order> getListByShopAndPage(@Param("shopId") Long shopId, Pageable pageable);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.shop.id = :shopId AND (:status IS NULL OR o.status = :status) AND (:orderCode IS NULL OR o.orderCode = :orderCode) ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND (:status IS NULL OR o.status = :status) AND (:orderCode IS NULL OR o.orderCode = :orderCode)")
    Page<Order> getListByShopAndStatusAndOrderCode(
            @Param("shopId") Long shopId,
            @Param("status") OrderStatus status,
            @Param("orderCode") String orderCode,
            Pageable pageable
    );

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.customer WHERE o.shop.id = :shopId AND o.createdAt >= :startAt AND o.createdAt < :endAt ORDER BY o.id ASC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.createdAt >= :startAt AND o.createdAt < :endAt")
    Page<Order> getListByShopAndMonth(
            @Param("shopId") Long shopId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"customer", "items", "shippingAddress"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByPaymentRef(String paymentRef);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopId(Long shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "shop", "shippingAddress"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "shop", "shippingAddress"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

        @Query("SELECT COALESCE(SUM(o.subtotal), 0) FROM Order o")
        BigDecimal sumTotalRevenue();

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate")
    List<Order> findOrdersSince(@Param("startDate") LocalDateTime startDate);
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

