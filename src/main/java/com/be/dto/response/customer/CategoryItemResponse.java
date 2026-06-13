package com.be.dto.response.customer;

public record CategoryItemResponse(
        Long id,
        String name,
        String slug,
        String iconUrl,
        Integer sortOrder
) {
}

