package com.be.dto.response.customer;

public record ProductDetailImageResponse(
        Long id,
        String url,
        Integer sortOrder,
        Boolean isPrimary
) {
}

