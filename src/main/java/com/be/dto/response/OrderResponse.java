package com.be.dto.response;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shopName;
    private String shippingAddress;
    private String shippingCity;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal discount;
    private BigDecimal total;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusLogResponse> statusLogs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderStatusLogResponse {
        private Long id;
        private String status;
        private String note;
        private LocalDateTime createdAt;
    }

    public static OrderResponse fromEntity(Order order) {
        String shippingAddr = "";
        String shippingCity = "";
        
        if (order.getShippingAddress() != null) {
            shippingAddr = order.getShippingAddress().getAddressDetail() + ", " 
                    + order.getShippingAddress().getWard() + ", "
                    + order.getShippingAddress().getDistrict();
            shippingCity = order.getShippingAddress().getProvince();
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = order.getSubtotal().add(order.getShippingFee()).subtract(discount);

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Unknown")
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : "")
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : "")
                .shopName(order.getShop() != null ? order.getShop().getName() : "Unknown Shop")
                .shippingAddress(shippingAddr)
                .shippingCity(shippingCity)
                .subtotal(order.getSubtotal())
                .shipping(order.getShippingFee())
                .discount(discount)
                .total(total)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paidAt(order.getPaidAt())
                .deliveredAt(order.getDeliveredAt())
                .items(order.getItems() != null ? order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .productName(item.getProductName())
                                .price(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()) : java.util.List.of())
                .statusLogs(order.getStatusLogs() != null ? order.getStatusLogs().stream()
                        .map(log -> OrderStatusLogResponse.builder()
                                .id(log.getId())
                                .status(log.getStatus().name())
                                .note(log.getNote())
                                .createdAt(log.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()) : java.util.List.of())
                .build();
    }
}
