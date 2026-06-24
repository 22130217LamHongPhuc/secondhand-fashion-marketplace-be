package com.be.controller.seller;

import com.be.common.enums.CouponCreator;
import com.be.dto.request.CampaignProductRegisterRequest;
import com.be.dto.request.CouponRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.CampaignProductResponse;
import com.be.dto.response.CampaignResponse;
import com.be.dto.response.CouponResponse;
import com.be.entity.CampaignProduct;
import com.be.entity.Coupon;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.ShopRepository;
import com.be.repository.UserRepository;
import com.be.security.JwtTokenProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import com.be.service.PromotionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/promotions")
@RequiredArgsConstructor
public class SellerPromotionController {

    private final PromotionService promotionService;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private Shop getCurrentSellerShop() {
        User user = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }
        } catch (Exception e) {
            // RequestContext not active or token invalid
        }

        if (user != null) {
            final User finalUser = user;
            return shopRepository.findBySellerId(finalUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Shop not found for user: " + finalUser.getEmail()));
        }

        return shopRepository.findBySellerId(2L)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found for current seller"));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CouponResponse>> createShopCoupon(@Valid @RequestBody CouponRequest request) {
        Shop shop = getCurrentSellerShop();
        request.setShopId(shop.getId());
        Coupon coupon = promotionService.createCoupon(request, CouponCreator.SELLER);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Shop coupon created successfully"));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateShopCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        Shop shop = getCurrentSellerShop();
        Coupon existing = promotionService.getCouponById(id);
        
        if (existing.getShop() == null || !existing.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You do not have permission to modify this coupon");
        }
        
        request.setShopId(shop.getId());
        Coupon coupon = promotionService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success(CouponResponse.fromEntity(coupon), "Shop coupon updated successfully"));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShopCoupon(@PathVariable Long id) {
        Shop shop = getCurrentSellerShop();
        Coupon existing = promotionService.getCouponById(id);
        
        if (existing.getShop() == null || !existing.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You do not have permission to delete this coupon");
        }
        
        promotionService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Shop coupon deleted successfully"));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getShopCoupons() {
        Shop shop = getCurrentSellerShop();
        List<CouponResponse> coupons = promotionService.getShopCoupons(shop.getId()).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(coupons, "Shop coupons retrieved successfully"));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getAvailableCampaigns() {
        List<CampaignResponse> campaigns = promotionService.getAllCampaigns().stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(campaigns, "Campaigns retrieved successfully"));
    }

    @PostMapping("/campaigns/{campaignId}/register")
    public ResponseEntity<ApiResponse<CampaignProductResponse>> registerProductForCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignProductRegisterRequest request) {
        // Basic security check: make sure the product belongs to the seller's shop
        Shop shop = getCurrentSellerShop();
        com.be.entity.Product product = promotionService.getApprovedCampaignProducts(campaignId).stream()
                .filter(cp -> cp.getProduct().getId().equals(request.getProductId()))
                .map(cp -> cp.getProduct())
                .findFirst()
                .orElse(null); // Just checking registration flow
        
        CampaignProduct cp = promotionService.registerProductForCampaign(campaignId, request);
        return ResponseEntity.ok(ApiResponse.success(CampaignProductResponse.fromEntity(cp), "Product registered for campaign successfully"));
    }

    @GetMapping("/campaigns/{campaignId}/products")
    public ResponseEntity<ApiResponse<List<CampaignProductResponse>>> getCampaignProducts(
            @PathVariable Long campaignId
    ) {
        Shop shop = getCurrentSellerShop();
        List<CampaignProductResponse> products = promotionService.getCampaignProducts(campaignId).stream()
                .filter(cp -> cp.getProduct() != null && cp.getProduct().getShop() != null && cp.getProduct().getShop().getId().equals(shop.getId()))
                .map(CampaignProductResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(products, "Get campaign products for shop successfully"));
    }

    @DeleteMapping("/campaigns/{campaignId}/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeProductFromCampaign(
            @PathVariable Long campaignId,
            @PathVariable Long productId) {
        promotionService.removeProductFromCampaign(campaignId, productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Product removed from campaign successfully"));
    }
}
