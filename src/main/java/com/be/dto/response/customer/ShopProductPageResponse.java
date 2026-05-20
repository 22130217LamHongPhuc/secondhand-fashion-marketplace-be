package com.be.dto.response.customer;

import java.util.List;

public record ShopProductPageResponse(
        List<ProductCardResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}

