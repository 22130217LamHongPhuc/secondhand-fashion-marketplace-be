package com.be.dto.response.seller;

public record ProductImageResponse(
    Long id,
    String url,
    int sortOrder,
    boolean isPrimary
) {}
