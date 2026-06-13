package com.be.dto.response.seller;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailResponse(
    Long id,
    String orderCode,
    String customerName,
    String status,
    String statusLabel,
    BigDecimal subtotal,
    BigDecimal shippingFee,
    String formattedSubtotal,
    String formattedTotal,
    String paymentMethod,
    String paymentMethodLabel,
    String paymentStatus,
    String paymentStatusLabel,
    String cancelReason,
    String paidAt,
    String deliveredAt,
    String createdAt,
    ShippingAddressResponse shippingAddress,
    List<OrderItemResponse> items
) {}
