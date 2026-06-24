package com.be.dto.response;

import com.be.common.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {
    @JsonProperty("isValid")
    private boolean isValid;
    private BigDecimal discountAmount;
    private String message;
    private DiscountType discountType;
    private BigDecimal discountValue;
}
