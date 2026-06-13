package com.be.dto.response.seller;

public record OrderItemResponse(
    String productName,
    String productThumbnail,
    int quantity,
    String formattedUnitPrice,
    String formattedSubtotal
) {}
