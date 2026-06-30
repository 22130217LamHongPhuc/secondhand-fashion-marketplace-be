package com.be.dto.response.customer;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.entity.Order;
import com.be.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public record OrderDetailResponse(
        Long id,
        String orderCode,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String cancelReason,

        ShopInfo shop,
        ShippingAddressInfo shippingAddress,

        List<OrderItemInfo> items,

        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        CouponInfo couponInfo,
        BigDecimal total,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime paidAt,
        LocalDateTime deliveredAt
) {

    public record ShopInfo(
            Long id,
            String name,
            String slug,
            String avatarUrl
    ) {}

    public record ShippingAddressInfo(
            Long id,
            String fullName,
            String phone,
            String province,
            String district,
            String ward,
            String addressDetail
    ) {}

    public record OrderItemInfo(
            Long id,
            Long productId,
            String productName,
            String thumbnailUrl,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal,
            Boolean reviewed
    ) {}

    /**
     * Thông tin mã giảm giá đã áp dụng cho đơn hàng.
     * source: "ADMIN_COUPON", "SHOP_COUPON", hoặc "SHOP_VOUCHER"
     */
    public record CouponInfo(
            String code,
            String name,
            String source
    ) {}

    public static OrderDetailResponse fromEntity(Order order) {
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = order.getSubtotal()
                .add(order.getShippingFee())
                .subtract(discount);

        ShopInfo shopInfo = null;
        if (order.getShop() != null) {
            shopInfo = new ShopInfo(
                    order.getShop().getId(),
                    order.getShop().getName(),
                    order.getShop().getSlug(),
                    order.getShop().getAvatarUrl()
            );
        }

        ShippingAddressInfo addressInfo = null;
        if (order.getShippingAddress() != null) {
            var addr = order.getShippingAddress();
            addressInfo = new ShippingAddressInfo(
                    addr.getId(),
                    addr.getFullName(),
                    addr.getPhone(),
                    addr.getProvince(),
                    addr.getDistrict(),
                    addr.getWard(),
                    addr.getAddressDetail()
            );
        }

        List<OrderItemInfo> itemInfos = List.of();
        if (order.getItems() != null) {
            // Build a set of reviewed productIds for this order
            var reviewedProductIds = order.getReviews() != null
                    ? order.getReviews().stream()
                            .map(r -> r.getProduct() != null ? r.getProduct().getId() : null)
                            .collect(java.util.stream.Collectors.toSet())
                    : java.util.Set.of();

            itemInfos = order.getItems().stream()
                    .map(item -> {
                        String thumbnailUrl = null;
                        if (item.getProduct() != null
                                && item.getProduct().getImages() != null
                                && !item.getProduct().getImages().isEmpty()) {
                            thumbnailUrl = item.getProduct().getImages().stream()
                                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                                    .findFirst()
                                    .map(img -> img.getUrl())
                                    .orElse(item.getProduct().getImages().get(0).getUrl());
                        }

                        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
                        boolean reviewed = productId != null && reviewedProductIds.contains(productId);

                        return new OrderItemInfo(
                                item.getId(),
                                productId,
                                item.getProductName(),
                                thumbnailUrl,
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getSubtotal(),
                                reviewed
                        );
                    })
                    .collect(Collectors.toList());
        }

        // Build coupon info
        CouponInfo couponInfo = null;
        if (discount.signum() > 0) {
            if (order.getCoupon() != null) {
                var coupon = order.getCoupon();
                String source = coupon.getShop() != null ? "SHOP_COUPON" : "ADMIN_COUPON";
                couponInfo = new CouponInfo(coupon.getCode(), coupon.getName(), source);
            } else if (order.getPromotion() != null) {
                var promo = order.getPromotion();
                couponInfo = new CouponInfo(promo.getCode(), promo.getName(), "SHOP_VOUCHER");
            }
        }

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getCancelReason(),
                shopInfo,
                addressInfo,
                itemInfos,
                order.getSubtotal(),
                order.getShippingFee(),
                discount,
                couponInfo,
                total,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPaidAt(),
                order.getDeliveredAt()
        );
    }
}
