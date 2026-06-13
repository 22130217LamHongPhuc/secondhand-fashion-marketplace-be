package com.be.dto.response.customer;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailReviewResponse(
        Long id,
        Byte rating,
        String comment,
        Long reviewerId,
        String reviewerName,
        String reviewerAvatarUrl,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
}

