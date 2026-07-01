package com.be.service.seller;

import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;

import java.time.LocalDate;
import java.util.List;

public interface SellerStatisticService {
    SellerDashboardResponse getDashboardData(String revenuePeriod, LocalDate startDate, LocalDate endDate);
    SellerAnalyticsResponse getAnalyticsData(int page, int size);
    List<SellerDashboardResponse.CategoryBreakdownEntry> getCategoryBreakdown(int month, int year);
}
