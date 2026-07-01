package com.be.repository;

import com.be.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SellerStatisticRepository extends JpaRepository<Shop, Long> {

    @Query(value = "SELECT COALESCE(SUM(subtotal), 0) FROM orders WHERE status = 'DONE' AND shop_id = :shopId", nativeQuery = true)
    BigDecimal getTotalRevenue(@Param("shopId") Long shopId);

    @Query(value = "SELECT COALESCE(SUM(subtotal), 0) FROM orders WHERE status = 'DONE' AND shop_id = :shopId AND created_at >= :startDateTime AND created_at < :endDateTime", nativeQuery = true)
    BigDecimal getRevenueByPeriod(@Param("shopId") Long shopId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING' AND shop_id = :shopId", nativeQuery = true)
    long countPendingOrders(@Param("shopId") Long shopId);

    @Query(value = "SELECT COUNT(*) FROM products WHERE is_active = true AND shop_id = :shopId", nativeQuery = true)
    long countActiveProducts(@Param("shopId") Long shopId);

    @Query(value = "SELECT COUNT(*) FROM products WHERE is_active = false AND shop_id = :shopId", nativeQuery = true)
    long countPendingProducts(@Param("shopId") Long shopId);

    @Query(value = """
            SELECT 
              c.name AS categoryName,
              SUM(oi.subtotal) AS totalSubtotal
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.status = 'DONE' AND o.shop_id = :shopId
            GROUP BY c.name
            ORDER BY totalSubtotal DESC
            """, nativeQuery = true)
    List<ICategoryDistributionProjection> getCategoryDistribution(@Param("shopId") Long shopId);

    @Query(value = """
            SELECT 
              c.name AS categoryName,
              SUM(oi.subtotal) AS totalSubtotal
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.status = 'DONE' AND o.shop_id = :shopId
              AND o.created_at >= :startDateTime AND o.created_at < :endDateTime
            GROUP BY c.name
            ORDER BY totalSubtotal DESC
            """, nativeQuery = true)
    List<ICategoryDistributionProjection> getCategoryDistributionByPeriod(
            @Param("shopId") Long shopId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query(value = """
            SELECT 
              COALESCE(SUM(CASE WHEN DAY(o.created_at) BETWEEN 1 AND 7 THEN o.subtotal ELSE 0 END), 0) AS week1,
              COALESCE(SUM(CASE WHEN DAY(o.created_at) BETWEEN 8 AND 14 THEN o.subtotal ELSE 0 END), 0) AS week2,
              COALESCE(SUM(CASE WHEN DAY(o.created_at) BETWEEN 15 AND 21 THEN o.subtotal ELSE 0 END), 0) AS week3,
              COALESCE(SUM(CASE WHEN DAY(o.created_at) >= 22 THEN o.subtotal ELSE 0 END), 0) AS week4
            FROM orders o
            WHERE o.status = 'DONE' 
              AND o.shop_id = :shopId 
              AND o.created_at >= :startDateTime 
              AND o.created_at < :endDateTime
            """, nativeQuery = true)
    IWeeklyRevenueProjection getWeeklyRevenue(@Param("shopId") Long shopId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = """
            SELECT 
              osl.status AS status,
              o.order_code AS orderCode,
              o.cancel_reason AS cancelReason,
              osl.changed_by AS changedBy,
              osl.created_at AS createdAt
            FROM order_status_logs osl
            JOIN orders o ON osl.order_id = o.id
            WHERE o.shop_id = :shopId 
              AND (osl.status = 'DONE' OR (osl.status = 'CANCELLED' AND osl.changed_by = :sellerId))
            ORDER BY osl.created_at ASC
            """, nativeQuery = true)
    List<IReputationLogProjection> getReputationLogs(@Param("shopId") Long shopId, @Param("sellerId") Long sellerId);

    // Pending Orders for Recent Customer Avatars and Notifications
    @Query(value = """
            SELECT 
              o.order_code AS orderCode,
              u.full_name AS customerName,
              u.avatar_url AS avatarUrl,
              o.created_at AS createdAt,
              (SELECT p.name FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = o.id LIMIT 1) AS firstProductName
            FROM orders o
            JOIN users u ON o.customer_id = u.id
            WHERE o.status = 'PENDING' AND o.shop_id = :shopId
            ORDER BY o.created_at DESC
            """, nativeQuery = true)
    List<IRecentOrderProjection> getRecentPendingOrders(@Param("shopId") Long shopId);

    // Projections
    interface ICategoryDistributionProjection {
        String getCategoryName();
        BigDecimal getTotalSubtotal();
    }

    interface IWeeklyRevenueProjection {
        BigDecimal getWeek1();
        BigDecimal getWeek2();
        BigDecimal getWeek3();
        BigDecimal getWeek4();
    }

    interface IReputationLogProjection {
        String getStatus();
        String getOrderCode();
        String getCancelReason();
        Long getChangedBy();
        LocalDateTime getCreatedAt();
    }

    interface IRecentOrderProjection {
        String getOrderCode();
        String getCustomerName();
        String getAvatarUrl();
        LocalDateTime getCreatedAt();
        String getFirstProductName();
    }
}
