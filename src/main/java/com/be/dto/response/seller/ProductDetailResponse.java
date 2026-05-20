package com.be.dto.response.seller;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
    Long id,
    String name,
    String description,
    String brand,
    String originCountry,
    String condition,
    String conditionLabel,
    BigDecimal basePrice,
    BigDecimal salePrice,
    int stockQuantity,
    boolean isActive,
    BigDecimal ratingAvg,
    int totalReviews,
    String createdAt,
    String updatedAt,
    List<ProductImageResponse> images,
    List<ProductAttributeResponse> attributes,
    List<String> tags
) {}
