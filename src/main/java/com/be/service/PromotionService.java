package com.be.service;

import com.be.common.enums.CampaignProductStatus;
import com.be.common.enums.CouponCreator;
import com.be.dto.request.CampaignProductRegisterRequest;
import com.be.dto.request.CampaignRequest;
import com.be.dto.request.CouponRequest;
import com.be.entity.Campaign;
import com.be.entity.CampaignProduct;
import com.be.entity.Coupon;

import java.util.List;

public interface PromotionService {
    // Coupon Management
    Coupon createCoupon(CouponRequest request, CouponCreator creator);
    Coupon updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
    Coupon toggleCouponActive(Long id, boolean active);
    List<Coupon> getAllCoupons();
    List<Coupon> getShopCoupons(Long shopId);
    Coupon getCouponByCode(String code);
    Coupon getCouponById(Long id);

    // Campaign Management
    Campaign createCampaign(CampaignRequest request);
    Campaign updateCampaign(Long id, CampaignRequest request);
    void deleteCampaign(Long id);
    Campaign toggleCampaignActive(Long id, boolean active);
    List<Campaign> getAllCampaigns();
    List<Campaign> getActiveCampaigns();
    Campaign getCampaignById(Long id);

    // Campaign Product Management
    CampaignProduct registerProductForCampaign(Long campaignId, CampaignProductRegisterRequest request);
    CampaignProduct updateCampaignProductStatus(Long campaignId, Long productId, CampaignProductStatus status);
    List<CampaignProduct> getCampaignProducts(Long campaignId);
    List<CampaignProduct> getApprovedCampaignProducts(Long campaignId);
    void removeProductFromCampaign(Long campaignId, Long productId);
    com.be.dto.response.CouponValidationResponse validateCoupon(String code, java.math.BigDecimal subtotal);
}

