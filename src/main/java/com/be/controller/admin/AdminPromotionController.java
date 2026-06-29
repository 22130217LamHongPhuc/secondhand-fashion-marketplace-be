package com.be.controller;

import com.be.common.enums.CouponCreator;
import com.be.dto.request.CouponRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.CouponResponse;
import com.be.entity.Coupon;
import com.be.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

}
