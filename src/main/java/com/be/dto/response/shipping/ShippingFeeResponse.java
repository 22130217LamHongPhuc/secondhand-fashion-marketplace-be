package com.be.dto.response.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingFeeResponse {
    private BigDecimal totalFee;
    private Map<Long, BigDecimal> shopFees;
    private boolean fallbackUsed;
    private String message;
}
