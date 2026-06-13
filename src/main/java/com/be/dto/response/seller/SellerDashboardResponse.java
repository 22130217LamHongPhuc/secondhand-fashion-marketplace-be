package com.be.dto.response.seller;

import java.math.BigDecimal;
import java.util.List;

public record SellerDashboardResponse(
        DashboardSummary summary,
        List<RevenueChartEntry> revenueChart,
        List<CategoryBreakdownEntry> categoryBreakdown,
        List<NotificationEntry> recentNotifications
) {
    public record DashboardSummary(
            BigDecimal totalRevenue,
            Double revenueGrowthPercentage,
            List<BigDecimal> revenueTrend,
            Long pendingOrdersCount,
            List<String> recentCustomerAvatars,
            Long extraOrdersCount,
            Long totalProducts,
            Long activeProductsCount,
            Long pendingProductsCount
    ) {}

    public record RevenueChartEntry(
            String label,
            BigDecimal light,
            BigDecimal dark
    ) {}

    public record CategoryBreakdownEntry(
            String label,
            Integer percent,
            String color
    ) {}

    public record NotificationEntry(
            Long id,
            String type,
            String title,
            String desc,
            String time
    ) {}
}
