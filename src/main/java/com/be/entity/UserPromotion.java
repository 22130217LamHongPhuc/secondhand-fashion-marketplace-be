package com.be.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Liên kết tới đối tượng Khuyến mãi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    // Số lần user này thực tế đã dùng mã này (để check với limitPerUser của Promotion)
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;

    // Thời điểm người dùng nhấn "Claim" mã về ví
    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @PrePersist
    protected void onClaim() {
        claimedAt = LocalDateTime.now();
    }
}