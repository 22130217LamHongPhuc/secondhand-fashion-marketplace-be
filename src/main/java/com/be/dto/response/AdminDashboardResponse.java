package com.be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalSellers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long activeUsers;
    private long pendingOrders;
    private java.util.List<OrderSummary> recentOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderSummary {
        private String id;
        private String customerName;
        private java.math.BigDecimal total;
        private String status;
    }
}
