package com.be.dto.response.seller;

public record OrderListResponse(
    Long id,
    String orderCode,
    String customerName,
    String status,
    String statusLabel,
    String formattedSubtotal,
    String formattedTotal,
    String formattedDate,
    String paymentMethodLabel,
    String paymentStatusLabel
) {}
