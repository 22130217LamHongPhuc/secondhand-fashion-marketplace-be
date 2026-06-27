package com.be.controller.customer;

import com.be.entity.Promotion;
import com.be.entity.UserPromotion;
import com.be.service.customer.CustomerPromotionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotions")
@AllArgsConstructor
public class CustomerPromotionController {

    private final CustomerPromotionService customerPromotionService;

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<Page<Promotion>> getAvailablePromotions(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(customerPromotionService.getAvailablePromotions(shopId, page, size));
    }

    @PostMapping("/{promotionId}/claim")
    public ResponseEntity<UserPromotion> claimPromotion(
            @RequestAttribute(name = "userId", required = false) Long userId, // NOTE: Có thể thay thế bằng @AuthenticationPrincipal hoặc custom annotation
            @PathVariable Long promotionId) {
        return new ResponseEntity<>(customerPromotionService.claimPromotion(userId, promotionId), HttpStatus.CREATED);
    }

    @GetMapping("/my-wallet")
    public ResponseEntity<Page<UserPromotion>> getMyWallet(
            @RequestAttribute(name = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(customerPromotionService.getMyWallet(userId, page, size));
    }
}
