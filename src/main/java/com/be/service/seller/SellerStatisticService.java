package com.be.service.seller;

import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;

public interface SellerStatisticService {
    SellerDashboardResponse getDashboardData(Long shopId, String revenuePeriod);
    SellerAnalyticsResponse getAnalyticsData(Long shopId, int page, int size);
}
