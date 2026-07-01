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
    private long totalCustomers;
    private long totalAdmins;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long activeUsers;
    private long pendingOrders;

    // New fields
    private long totalShops;
    private long activeShops;
    private long verifiedShops;

    private long confirmedOrders;
    private long shippingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long returnedOrders;

    private double cancellationRate;
    private double returnRate;

    private double userGrowth;
    private double orderGrowth;
    private double revenueGrowth;
    private double shopGrowth;

    private long pendingComplaints;

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
