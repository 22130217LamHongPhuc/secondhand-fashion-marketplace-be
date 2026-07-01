package com.be.controller;

import com.be.dto.response.ApiResponse;
import com.be.dto.response.HomeBannerResponse;
import com.be.service.HomeBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class HomeBannerController {

    private final HomeBannerService homeBannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeBannerResponse>>> getActiveBanners() {
        List<HomeBannerResponse> activeBanners = homeBannerService.getActiveBanners().stream()
                .map(HomeBannerResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(activeBanners, "Active home banners retrieved successfully"));
    }
}
