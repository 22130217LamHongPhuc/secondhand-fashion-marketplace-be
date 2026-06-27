package com.be.entity;

import com.be.constant.DiscountType;
import com.be.constant.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_shop_code", columnList = "shop_id, code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // Mã giảm giá, ví dụ: SHOP50K, WELCOME2026.
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    // Giá trị giảm giá (Số tiền hoặc số Phần trăm)
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    // Số tiền giảm tối đa (Bắt buộc dùng khi chọn PERCENTAGE để tránh lỗ nặng)
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    // ĐIỀU KIỆN ÁP DỤNG (RULES)
    // 1. Giá trị đơn hàng tối thiểu
    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    // 2. Số lượng món đồ tối thiểu trong đơn hàng (ví dụ: mua từ 2 món trở lên)
    @Column(name = "min_order_items")
    private Integer minOrderItems;

    // QUẢN LÝ SỐ LƯỢNG
    // Tổng số lượng mã có thể sử dụng cho chiến dịch này
    @Column(nullable = false)
    private Integer quantity;

    // Số lượng mã thực tế ĐÃ SỬ DỤNG (được tính khi đơn hàng thanh toán thành công)
    @Column(name = "used_quantity", nullable = false)
    @Builder.Default
    private Integer usedQuantity = 0;

    // THỜI GIAN HIỆU LỰC
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromotionStatus status = PromotionStatus.DRAFT;

    // AUDIT TIMESTAMPS
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}