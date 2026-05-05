package com.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.be.common.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_logs", indexes = {
    @Index(name = "idx_order_logs_order", columnList = "order_id"),
    @Index(name = "fk_order_logs_user", columnList = "changed_by")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLog {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String note;

    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = true)
    private User changedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

