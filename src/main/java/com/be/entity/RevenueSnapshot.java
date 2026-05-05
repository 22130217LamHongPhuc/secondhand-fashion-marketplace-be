package com.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenue_snapshots", uniqueConstraints = {
    @UniqueConstraint(name = "uq_revenue_shop_date", columnNames = {"shop_id", "snapshot_date"})
}, indexes = {
    @Index(name = "idx_revenue_date", columnList = "snapshot_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueSnapshot {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_items_sold", nullable = false)
    @Builder.Default
    private Integer totalItemsSold = 0;

    @Column(name = "platform_fee", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "net_revenue", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;
}

