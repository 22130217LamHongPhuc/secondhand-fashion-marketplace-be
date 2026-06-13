package com.be.dto.response.seller;

public record OrderActionResponse(
    Long id,
    String status,
    String statusLabel
) {}
