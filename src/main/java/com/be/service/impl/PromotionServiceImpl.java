package com.be.service.impl;

import com.be.common.enums.CampaignProductStatus;
import com.be.common.enums.CouponCreator;
import com.be.dto.request.CampaignProductRegisterRequest;
import com.be.dto.request.CampaignRequest;
import com.be.dto.request.CouponRequest;
import com.be.entity.*;
import com.be.repository.*;
import com.be.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final CouponRepository couponRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignProductRepository campaignProductRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    // ==========================================
    // COUPON MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public Coupon createCoupon(CouponRequest request, CouponCreator creator) {
        if (couponRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new RuntimeException("Coupon code already exists: " + request.getCode());
        }

        Shop shop = null;
        if (creator == CouponCreator.SELLER && request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop not found with id: " + request.getShopId()));
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(creator)
                .shop(shop)
                .isActive(true)
                .isAutoSave(request.getIsAutoSave() != null ? request.getIsAutoSave() : false)
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public Coupon updateCoupon(Long id, CouponRequest request) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        // Code changes validation
        if (!existing.getCode().equalsIgnoreCase(request.getCode())) {
            couponRepository.findByCode(request.getCode().toUpperCase())
                    .ifPresent(c -> {
                        throw new RuntimeException("Coupon code already exists: " + request.getCode());
                    });
            existing.setCode(request.getCode().toUpperCase().trim());
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setDiscountType(request.getDiscountType());
        existing.setDiscountValue(request.getDiscountValue());
        existing.setMinOrderValue(request.getMinOrderValue());
        existing.setMaxDiscountAmount(request.getMaxDiscountAmount());
        existing.setUsageLimit(request.getUsageLimit());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        if (request.getIsAutoSave() != null) {
            existing.setIsAutoSave(request.getIsAutoSave());
        }

        return couponRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Coupon toggleCouponActive(Long id, boolean active) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
        coupon.setIsActive(active);
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public List<Coupon> getShopCoupons(Long shopId) {
        return couponRepository.findByShopId(shopId);
    }

    @Override
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
    }

    @Override
    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
    }

    // ==========================================
    // CAMPAIGN MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public Campaign createCampaign(CampaignRequest request) {
        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .isAutoSave(request.getIsAutoSave() != null ? request.getIsAutoSave() : false)
                .build();
        return campaignRepository.save(campaign);
    }

    @Override
    @Transactional
    public Campaign updateCampaign(Long id, CampaignRequest request) {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setBannerUrl(request.getBannerUrl());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        if (request.getIsAutoSave() != null) {
            existing.setIsAutoSave(request.getIsAutoSave());
        }

        return campaignRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new RuntimeException("Campaign not found with id: " + id);
        }
        campaignRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Campaign toggleCampaignActive(Long id, boolean active) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        campaign.setIsActive(active);
        return campaignRepository.save(campaign);
    }

    @Override
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    @Override
    public List<Campaign> getActiveCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        return campaignRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
    }

    @Override
    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
    }

    // ==========================================
    // CAMPAIGN PRODUCT MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public CampaignProduct registerProductForCampaign(Long campaignId, CampaignProductRegisterRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + campaignId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Check if already registered
        campaignProductRepository.findByCampaignIdAndProductId(campaignId, request.getProductId())
                .ifPresent(cp -> {
                    throw new RuntimeException("Product is already registered in this campaign");
                });

        CampaignProduct cp = CampaignProduct.builder()
                .campaign(campaign)
                .product(product)
                .campaignPrice(request.getCampaignPrice())
                .status(CampaignProductStatus.PENDING)
                .build();

        return campaignProductRepository.save(cp);
    }

    @Override
    @Transactional
    public CampaignProduct updateCampaignProductStatus(Long campaignId, Long productId, CampaignProductStatus status) {
        CampaignProduct cp = campaignProductRepository.findByCampaignIdAndProductId(campaignId, productId)
                .orElseThrow(() -> new RuntimeException("Campaign product not found"));

        cp.setStatus(status);

        // If approved, update the product's salePrice to the campaign price
        // If rejected, ensure it does not reflect the campaign price
        if (status == CampaignProductStatus.APPROVED) {
            Product product = cp.getProduct();
            product.setSalePrice(cp.getCampaignPrice());
            productRepository.save(product);
        } else if (status == CampaignProductStatus.REJECTED) {
            Product product = cp.getProduct();
            // Revert back or set to null if there was no other discount.
            // For now, setting it to null is standard.
            product.setSalePrice(null);
            productRepository.save(product);
        }

        return campaignProductRepository.save(cp);
    }

    @Override
    public List<CampaignProduct> getCampaignProducts(Long campaignId) {
        return campaignProductRepository.findByCampaignId(campaignId);
    }

    @Override
    public List<CampaignProduct> getApprovedCampaignProducts(Long campaignId) {
        return campaignProductRepository.findByCampaignIdAndStatus(campaignId, CampaignProductStatus.APPROVED);
    }

    @Override
    @Transactional
    public void removeProductFromCampaign(Long campaignId, Long productId) {
        CampaignProduct cp = campaignProductRepository.findByCampaignIdAndProductId(campaignId, productId)
                .orElseThrow(() -> new RuntimeException("Campaign product mapping not found"));
        
        // Revert product sale price if it was approved
        if (cp.getStatus() == CampaignProductStatus.APPROVED) {
            Product product = cp.getProduct();
            product.setSalePrice(null);
            productRepository.save(product);
        }

        campaignProductRepository.delete(cp);
    }

    @Override
    public com.be.dto.response.CouponValidationResponse validateCoupon(String code, java.math.BigDecimal subtotal) {
        java.util.Optional<Coupon> couponOpt = couponRepository.findByCode(code.toUpperCase().trim());
        if (couponOpt.isEmpty()) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Mã giảm giá không tồn tại")
                    .build();
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.getIsActive()) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Mã giảm giá đã bị vô hiệu hóa")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Mã giảm giá chưa đến thời gian sử dụng")
                    .build();
        }

        if (now.isAfter(coupon.getEndDate())) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Mã giảm giá đã hết hạn sử dụng")
                    .build();
        }

        int usedCount = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
        if (coupon.getUsageLimit() != null && usedCount >= coupon.getUsageLimit()) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Mã giảm giá đã hết lượt sử dụng")
                    .build();
        }

        if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            return com.be.dto.response.CouponValidationResponse.builder()
                    .isValid(false)
                    .message("Giá trị đơn hàng chưa đạt tối thiểu (yêu cầu từ " + coupon.getMinOrderValue() + "đ)")
                    .build();
        }

        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
        if (coupon.getDiscountType() == com.be.common.enums.DiscountType.PERCENTAGE) {
            // discount = subtotal * value / 100
            discountAmount = subtotal.multiply(coupon.getDiscountValue()).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discountAmount = coupon.getMaxDiscountAmount();
            }
        } else if (coupon.getDiscountType() == com.be.common.enums.DiscountType.FIXED_AMOUNT) {
            discountAmount = coupon.getDiscountValue();
        }

        // Discount cannot be larger than subtotal
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        return com.be.dto.response.CouponValidationResponse.builder()
                .isValid(true)
                .discountAmount(discountAmount)
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .message("Áp dụng mã giảm giá thành công")
                .build();
    }
}

