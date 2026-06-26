package com.be.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        String brand,
        String originCountry,
        String condition,
        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal discountAmount,
        Integer stockQuantity,
        BigDecimal ratingAvg,
        Integer totalReviews,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String thumbnailUrl,
        List<ProductDetailImageResponse> images,
        List<ProductDetailAttributeResponse> attributes,
        List<String> tags,
        ProductDetailCategoryResponse category,
        ProductDetailShopResponse shop,
        List<ProductDetailCommentResponse> latestComments,
        List<ProductDetailReviewResponse> latestReviews,
        List<ProductCardResponse> relatedProducts,
        Map<String, String> metadata
) {
}

