package com.be.entity;

import com.be.common.enums.CouponCreator;
import com.be.common.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", uniqueConstraints = {
    @UniqueConstraint(name = "uq_coupons_code", columnNames = "code")
}, indexes = {
    @Index(name = "idx_coupons_code", columnList = "code"),
    @Index(name = "idx_coupons_shop", columnList = "shop_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false)
    private CouponCreator createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = true)
    @JsonIgnore
    private Shop shop;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_auto_save", nullable = false)
    @Builder.Default
    private Boolean isAutoSave = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
