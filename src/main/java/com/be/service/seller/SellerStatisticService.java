package com.be.service.seller;

import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;

import java.time.LocalDate;

public interface SellerStatisticService {
    SellerDashboardResponse getDashboardData(Long shopId, String revenuePeriod, LocalDate startDate, LocalDate endDate);
    SellerAnalyticsResponse getAnalyticsData(Long shopId, int page, int size);
}
