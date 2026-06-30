package com.be.dto.response.customer;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderHistoryItemResponse(
        Long id,
        String orderCode,
        String shopName,
        String shopAvatarUrl,
        Long shopId,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        String couponCode,
        BigDecimal total,
        int itemCount,
        String thumbnailUrl,
        String firstProductName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static OrderHistoryItemResponse fromEntity(Order order) {
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = order.getSubtotal()
                .add(order.getShippingFee())
                .subtract(discount);

        String thumbnailUrl = null;
        String firstProductName = null;
        int itemCount = 0;

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            itemCount = order.getItems().size();
            var firstItem = order.getItems().get(0);
            firstProductName = firstItem.getProductName();

            if (firstItem.getProduct() != null
                    && firstItem.getProduct().getImages() != null
                    && !firstItem.getProduct().getImages().isEmpty()) {
                thumbnailUrl = firstItem.getProduct().getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .map(img -> img.getUrl())
                        .orElse(firstItem.getProduct().getImages().get(0).getUrl());
            }
        }

        // Resolve coupon code from either Coupon or Promotion
        String couponCode = null;
        if (discount.signum() > 0) {
            if (order.getCoupon() != null) {
                couponCode = order.getCoupon().getCode();
            } else if (order.getPromotion() != null) {
                couponCode = order.getPromotion().getCode();
            }
        }

        return new OrderHistoryItemResponse(
                order.getId(),
                order.getOrderCode(),
                order.getShop() != null ? order.getShop().getName() : null,
                order.getShop() != null ? order.getShop().getAvatarUrl() : null,
                order.getShop() != null ? order.getShop().getId() : null,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                discount,
                couponCode,
                total,
                itemCount,
                thumbnailUrl,
                firstProductName,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
