package com.be.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopInfoResponse(
        Long id,
        String name,
        String slug,
        String description,
        String avatarUrl,
        String bannerUrl,
        BigDecimal ratingAvg,
        Integer totalReviews,
        Boolean isActive,
        Boolean isVerified,
        LocalDateTime createdAt,
        Integer provinceId,
        String provinceName,
        Integer districtId,
        String districtName,
        String wardCode,
        String wardName,
        String addressDetail
) {
}


