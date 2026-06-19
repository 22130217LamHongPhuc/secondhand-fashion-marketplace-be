package com.be.dto.request;

import com.be.common.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotBlank(message = "Coupon name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Long shopId; // nullable (null means Admin coupon)

    private Boolean isAutoSave;
}
