package com.be.controller.seller;

import com.be.constant.PromotionStatus;
import com.be.dto.request.seller.PromotionCreateDTO;
import com.be.dto.request.seller.PromotionUpdateDTO;
import com.be.entity.Promotion;
import com.be.service.seller.ShopPromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/seller/promotions")
@RequiredArgsConstructor
public class SellerPromotionController {

    private final ShopPromotionService shopPromotionService;

    @PostMapping
    public ResponseEntity<Promotion> createPromotion(
            @Valid @RequestBody PromotionCreateDTO request) {
        return new ResponseEntity<>(shopPromotionService.createPromotion(request), HttpStatus.CREATED);
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<Promotion> updatePromotion(
            @PathVariable Long promotionId,
            @Valid @RequestBody PromotionUpdateDTO request) {
        return ResponseEntity.ok(shopPromotionService.updatePromotion(promotionId, request));
    }

    @GetMapping
    public ResponseEntity<Page<Promotion>> getPromotionsByShop(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(shopPromotionService.getPromotionsByShop(keyword, fromDate, toDate, minPrice, maxPrice, page, size));
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<Promotion> getPromotionDetail(@PathVariable Long promotionId) {
        return ResponseEntity.ok(shopPromotionService.getPromotionDetail(promotionId));
    }

    @PatchMapping("/{promotionId}/status")
    public ResponseEntity<Promotion> changePromotionStatus(
            @PathVariable Long promotionId,
            @RequestParam PromotionStatus status) {
        return ResponseEntity.ok(shopPromotionService.changePromotionStatus(promotionId, status));
    }
}
