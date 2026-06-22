package com.be.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignProductRegisterRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Campaign sale price is required")
    private BigDecimal campaignPrice;
}
