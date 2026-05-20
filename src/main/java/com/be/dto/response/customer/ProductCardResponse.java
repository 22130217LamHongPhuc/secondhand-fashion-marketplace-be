package com.be.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductCardResponse(
        Long id,
        String name,
        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal discountAmount,
        String thumbnailUrl,
        Long shopId,
        String shopName,
        LocalDateTime createdAt
) {
}

