package com.be.controller.admin;

import com.be.dto.request.HomeBannerRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.HomeBannerResponse;
import com.be.entity.HomeBanner;
import com.be.service.HomeBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminHomeBannerController {

    private final HomeBannerService homeBannerService;

    @PostMapping
    public ResponseEntity<ApiResponse<HomeBannerResponse>> createBanner(@Valid @RequestBody HomeBannerRequest request) {
        HomeBanner banner = homeBannerService.createBanner(request);
        return ResponseEntity.ok(ApiResponse.success(HomeBannerResponse.fromEntity(banner), "Banner created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HomeBannerResponse>> updateBanner(@PathVariable Long id, @Valid @RequestBody HomeBannerRequest request) {
        HomeBanner banner = homeBannerService.updateBanner(id, request);
        return ResponseEntity.ok(ApiResponse.success(HomeBannerResponse.fromEntity(banner), "Banner updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        homeBannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Banner deleted successfully"));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<HomeBannerResponse>> toggleBannerActive(@PathVariable Long id, @RequestParam boolean active) {
        HomeBanner banner = homeBannerService.toggleBannerActive(id, active);
        return ResponseEntity.ok(ApiResponse.success(HomeBannerResponse.fromEntity(banner), "Banner status updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeBannerResponse>>> getAllBanners() {
        List<HomeBannerResponse> banners = homeBannerService.getAllBanners().stream()
                .map(HomeBannerResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(banners, "All banners retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HomeBannerResponse>> getBannerById(@PathVariable Long id) {
        HomeBanner banner = homeBannerService.getBannerById(id);
        return ResponseEntity.ok(ApiResponse.success(HomeBannerResponse.fromEntity(banner), "Banner retrieved successfully"));
    }
}
