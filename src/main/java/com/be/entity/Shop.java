package com.be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shops", uniqueConstraints = {
    @UniqueConstraint(name = "uq_shops_seller", columnNames = "seller_id"),
    @UniqueConstraint(name = "uq_shops_slug", columnNames = "slug")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "seller_id", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User seller;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;

    @Column(name = "avatar_url", columnDefinition = "TEXT", nullable = true)
    private String avatarUrl;

    @Column(name = "banner_url", columnDefinition = "TEXT", nullable = true)
    private String bannerUrl;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "warning_strikes", nullable = false)
    @Builder.Default
    private Integer warningStrikes = 0;

    // ── GHN Address Fields ───────────────────────────────────────
    @Column(name = "province_id", nullable = true)
    private Integer provinceId;

    @Column(name = "province_name", nullable = true, length = 100)
    private String provinceName;

    @Column(name = "district_id", nullable = true)
    private Integer districtId;

    @Column(name = "district_name", nullable = true, length = 100)
    private String districtName;

    @Column(name = "ward_code", nullable = true, length = 20)
    private String wardCode;

    @Column(name = "ward_name", nullable = true, length = 100)
    private String wardName;

    @Column(name = "address_detail", columnDefinition = "TEXT", nullable = true)
    private String addressDetail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Order> orders;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RevenueSnapshot> revenueSnapshots;
}

