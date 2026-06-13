package com.be.dto.response.seller;

public record ProductMutationResponse(
    Long id,
    String name,
    String displayStatus,
    String thumbnailUrl
) {}
