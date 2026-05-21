package com.be.dto.response.seller.mapper;

import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.UserAddress;
import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.dto.response.seller.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SellerOrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static OrderListResponse toListResponse(Order order) {
        if (order == null) return null;

        BigDecimal total = order.getSubtotal().add(order.getShippingFee());
        String customerName = (order.getCustomer() != null) ? order.getCustomer().getFullName() : "Khách hàng";

        return new OrderListResponse(
            order.getId(),
            order.getOrderCode(),
            customerName,
            order.getStatus() != null ? order.getStatus().name() : null,
            resolveStatusLabel(order.getStatus()),
            formatVND(order.getSubtotal()),
            formatVND(total),
            order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_ONLY_FORMATTER) : null,
            resolvePaymentMethodLabel(order.getPaymentMethod()),
            resolvePaymentStatusLabel(order.getPaymentStatus())
        );
    }

    public static OrderDetailResponse toDetailResponse(Order order) {
        if (order == null) return null;

        BigDecimal total = order.getSubtotal().add(order.getShippingFee());
        String customerName = (order.getCustomer() != null) ? order.getCustomer().getFullName() : "Khách hàng";

        ShippingAddressResponse shippingAddressResponse = null;
        UserAddress addr = order.getShippingAddress();
        if (addr != null) {
            String fullAddr = addr.getAddressDetail() + ", " + addr.getWard() + ", " + addr.getDistrict() + ", " + addr.getProvince();
            shippingAddressResponse = new ShippingAddressResponse(addr.getFullName(), addr.getPhone(), fullAddr);
        }

        List<OrderItemResponse> itemResponses = order.getItems() != null ?
            order.getItems().stream()
                .map(item -> {
                    String thumb = "";
                    if (item.getProduct() != null && item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        thumb = SellerProductMapper.resolveThumbnailUrl(item.getProduct().getImages());
                    }
                    return new OrderItemResponse(
                        item.getProductName(),
                        thumb,
                        item.getQuantity() != null ? item.getQuantity() : 0,
                        formatVND(item.getUnitPrice()),
                        formatVND(item.getSubtotal())
                    );
                })
                .toList() : Collections.emptyList();

        return new OrderDetailResponse(
            order.getId(),
            order.getOrderCode(),
            customerName,
            order.getStatus() != null ? order.getStatus().name() : null,
            resolveStatusLabel(order.getStatus()),
            order.getSubtotal(),
            order.getShippingFee(),
            formatVND(order.getSubtotal()),
            formatVND(total),
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            resolvePaymentMethodLabel(order.getPaymentMethod()),
            order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null,
            resolvePaymentStatusLabel(order.getPaymentStatus()),
            order.getCancelReason(),
            order.getPaidAt() != null ? order.getPaidAt().format(DATE_FORMATTER) : null,
            order.getDeliveredAt() != null ? order.getDeliveredAt().format(DATE_FORMATTER) : null,
            order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : null,
            shippingAddressResponse,
            itemResponses
        );
    }

    public static OrderActionResponse toActionResponse(Order order) {
        if (order == null) return null;
        return new OrderActionResponse(
            order.getId(),
            order.getStatus() != null ? order.getStatus().name() : null,
            resolveStatusLabel(order.getStatus())
        );
    }

    public static String formatVND(BigDecimal val) {
        if (val == null) return "0đ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(val) + "đ";
    }

    public static String resolveStatusLabel(OrderStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao";
            case CANCELLED -> "Đã hủy";
            case DONE -> "Hoàn thành";
        };
    }

    public static String resolvePaymentMethodLabel(PaymentMethod method) {
        if (method == null) return "";
        return switch (method) {
            case WALLET -> "Ví điện tử";
            case COD -> "Thanh toán khi nhận hàng";
            case BANK_TRANSFER -> "Chuyển khoản";
        };
    }

    public static String resolvePaymentStatusLabel(PaymentStatus status) {
        if (status == null) return "";
        return switch (status) {
            case UNPAID -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }
}
