package com.be.controller.seller;

import com.be.dto.response.ApiResponse;
import com.be.dto.response.seller.SellerAnalyticsResponse;
import com.be.dto.response.seller.SellerDashboardResponse;
import com.be.service.seller.SellerStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seller/stat")
@RequiredArgsConstructor
public class SellerStatisticController {
    private final SellerStatisticService sellerStatisticService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> getDashboard(
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "30_DAYS") String revenuePeriod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        SellerDashboardResponse data = sellerStatisticService.getDashboardData(shopId, revenuePeriod, startDate, endDate);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(data, "Get dashboard data successfully"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<SellerAnalyticsResponse>> getAnalytics(
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SellerAnalyticsResponse data = sellerStatisticService.getAnalyticsData(shopId, page, size);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(data, "Get analytics data successfully"));
    }
}
