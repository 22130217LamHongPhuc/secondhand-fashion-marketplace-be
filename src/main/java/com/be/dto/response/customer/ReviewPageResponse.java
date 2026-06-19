
package com.be.dto.response.customer;

import java.util.List;

public record ReviewPageResponse(
        List<ProductDetailReviewResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}

