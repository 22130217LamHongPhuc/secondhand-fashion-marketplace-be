package com.be.dto.response.seller;

public record ProductListResponse(
    Long id,
    String name,
    String brand,
    String conditionLabel,
    String formattedPrice,
    String formattedBasePrice,
    boolean hasDiscount,
    int stockQuantity,
    String displayStatus,
    String thumbnailUrl
) {}
