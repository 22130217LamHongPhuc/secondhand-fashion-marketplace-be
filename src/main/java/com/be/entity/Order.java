package com.be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_shop", columnList = "shop_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created", columnList = "created_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_orders_code", columnNames = "order_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private User customer;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    @Column(name = "order_code", nullable = false, length = 30, unique = true)
    private String orderCode;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id", nullable = true)
    private UserAddress shippingAddress;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = true)
    private Coupon coupon;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.WALLET;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "cancel_reason", columnDefinition = "TEXT", nullable = true)
    private String cancelReason;

    @Column(name = "paid_at", nullable = true)
    private LocalDateTime paidAt;

    @Column(name = "delivered_at", nullable = true)
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true , fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderStatusLog> statusLogs;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true , fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<WalletTransaction> walletTransactions;
}

