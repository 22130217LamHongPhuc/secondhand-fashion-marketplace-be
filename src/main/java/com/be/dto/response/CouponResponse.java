package com.be.dto.response;

import com.be.common.enums.CouponCreator;
import com.be.common.enums.DiscountType;
import com.be.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CouponCreator createdBy;
    private Long shopId;
    private String shopName;
    private Boolean isActive;
    private Boolean isAutoSave;
    private LocalDateTime createdAt;

    public static CouponResponse fromEntity(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .createdBy(coupon.getCreatedBy())
                .shopId(coupon.getShop() != null ? coupon.getShop().getId() : null)
                .shopName(coupon.getShop() != null ? coupon.getShop().getName() : null)
                .isActive(coupon.getIsActive())
                .isAutoSave(coupon.getIsAutoSave())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
