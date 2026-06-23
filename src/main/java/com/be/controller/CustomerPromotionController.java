package com.be.controller;

import com.be.dto.request.ValidateCouponRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.CouponResponse;
import com.be.dto.response.CouponValidationResponse;
import com.be.dto.response.CampaignResponse;
import com.be.entity.Coupon;
import com.be.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class CustomerPromotionController {

    private final PromotionService promotionService;

    @GetMapping("/coupons/available")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<CouponResponse> coupons = promotionService.getAllCoupons().stream()
                .filter(coupon -> Boolean.TRUE.equals(coupon.getIsActive()))
                .filter(coupon -> !now.isBefore(coupon.getStartDate()) && !now.isAfter(coupon.getEndDate()))
                .filter(coupon -> coupon.getUsageLimit() == null
                        || (coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) < coupon.getUsageLimit())
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(coupons, "Available coupons retrieved successfully"));
    }

    @GetMapping("/campaigns/active")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaigns() {
        List<CampaignResponse> campaigns = promotionService.getActiveCampaigns().stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(campaigns, "Active campaigns retrieved successfully"));
    }

    @PostMapping("/coupons/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResponse response = promotionService.validateCoupon(request.getCode(), request.getSubtotal());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
}
