package com.be.controller;

import com.be.common.enums.CampaignProductStatus;
import com.be.common.enums.CouponCreator;
import com.be.dto.request.CampaignRequest;
import com.be.dto.request.CouponRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.CampaignProductResponse;
import com.be.dto.response.CampaignResponse;
import com.be.dto.response.CouponResponse;
import com.be.entity.Campaign;
import com.be.entity.CampaignProduct;
import com.be.entity.Coupon;
import com.be.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;

    // ==========================================
    // COUPON MANAGEMENT
    // ==========================================

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        Coupon coupon = promotionService.createCoupon(request, CouponCreator.ADMIN);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Coupon created successfully"));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        Coupon coupon = promotionService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Coupon updated successfully"));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        promotionService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted successfully"));
    }

    @PatchMapping("/coupons/{id}/active")
    public ResponseEntity<ApiResponse<CouponResponse>> toggleCouponActive(@PathVariable Long id, @RequestParam boolean active) {
        Coupon coupon = promotionService.toggleCouponActive(id, active);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Coupon status updated successfully"));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        List<CouponResponse> coupons = promotionService.getAllCoupons().stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(coupons, "All coupons retrieved successfully"));
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        Coupon coupon = promotionService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Coupon retrieved successfully"));
    }

    // ==========================================
    // CAMPAIGN MANAGEMENT
    // ==========================================

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(@Valid @RequestBody CampaignRequest request) {
        Campaign campaign = promotionService.createCampaign(request);
        return ResponseEntity.ok(ApiResponse.success(CampaignResponse.fromEntity(campaign), "Campaign created successfully"));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(@PathVariable Long id, @Valid @RequestBody CampaignRequest request) {
        Campaign campaign = promotionService.updateCampaign(id, request);
        return ResponseEntity.ok(ApiResponse.success(CampaignResponse.fromEntity(campaign), "Campaign updated successfully"));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        promotionService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Campaign deleted successfully"));
    }

    @PatchMapping("/campaigns/{id}/active")
    public ResponseEntity<ApiResponse<CampaignResponse>> toggleCampaignActive(@PathVariable Long id, @RequestParam boolean active) {
        Campaign campaign = promotionService.toggleCampaignActive(id, active);
        return ResponseEntity.ok(ApiResponse.success(CampaignResponse.fromEntity(campaign), "Campaign status updated successfully"));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getAllCampaigns() {
        List<CampaignResponse> campaigns = promotionService.getAllCampaigns().stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(campaigns, "All campaigns retrieved successfully"));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(@PathVariable Long id) {
        Campaign campaign = promotionService.getCampaignById(id);
        return ResponseEntity.ok(ApiResponse.success(CampaignResponse.fromEntity(campaign), "Campaign retrieved successfully"));
    }

    // ==========================================
    // CAMPAIGN PRODUCTS REVIEW
    // ==========================================

    @GetMapping("/campaigns/{campaignId}/products")
    public ResponseEntity<ApiResponse<List<CampaignProductResponse>>> getCampaignProducts(@PathVariable Long campaignId) {
        List<CampaignProductResponse> products = promotionService.getCampaignProducts(campaignId).stream()
                .map(CampaignProductResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(products, "Campaign products retrieved successfully"));
    }

    @PatchMapping("/campaigns/{campaignId}/products/{productId}/status")
    public ResponseEntity<ApiResponse<CampaignProductResponse>> updateProductStatus(
            @PathVariable Long campaignId,
            @PathVariable Long productId,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        CampaignProductStatus status = CampaignProductStatus.valueOf(statusStr.toUpperCase());
        CampaignProduct cp = promotionService.updateCampaignProductStatus(campaignId, productId, status);
        return ResponseEntity.ok(ApiResponse.success(CampaignProductResponse.fromEntity(cp), "Product registration status updated successfully"));
    }
}
