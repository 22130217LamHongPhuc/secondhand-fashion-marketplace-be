package com.be.dto.response.customer;

import java.math.BigDecimal;

public record ProductDetailShopResponse(
        Long id,
        String name,
        String slug,
        String avatarUrl,
        Boolean isVerified,
        BigDecimal ratingAvg,
        Integer totalReviews,
        Long sellerId,
        Integer provinceId,
        String provinceName,
        Integer districtId,
        String districtName,
        String wardCode,
        String wardName,
        String addressDetail
) {

}



