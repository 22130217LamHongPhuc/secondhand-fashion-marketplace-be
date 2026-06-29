package com.be.service.seller.impl;

import com.be.constant.DiscountType;
import com.be.constant.PromotionStatus;
import com.be.dto.request.seller.PromotionCreateDTO;
import com.be.dto.request.seller.PromotionUpdateDTO;
import com.be.entity.Promotion;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.PromotionRepository;
import com.be.security.AuthHelper;
import com.be.service.seller.ShopPromotionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.be.repository.specification.PromotionSpecification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopPromotionServiceImpl implements ShopPromotionService {

    private final PromotionRepository promotionRepository;
    private final AuthHelper authHelper;

    @Override
    @Transactional
    public Promotion createPromotion(PromotionCreateDTO request) {
        Shop shop = authHelper.getCurrentSellerShop();

        // Validate dates
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // Validate discount
        if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0");
        }
        
        if (request.getDiscountType() == DiscountType.PERCENTAGE) {
            if (request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100%");
            }
            if (request.getMaxDiscountAmount() == null) {
                throw new IllegalArgumentException("Yêu cầu nhập số tiền giảm tối đa đối với giảm giá theo phần trăm");
            }
        }

        // Check for duplicate code in the same shop
        if (promotionRepository.existsByShop_IdAndCode(shop.getId(), request.getCode())) {
            throw new IllegalStateException("Mã khuyến mãi đã tồn tại trong cửa hàng của bạn");
        }

        Promotion promotion = Promotion.builder()
                .shop(shop)
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .minOrderItems(request.getMinOrderItems())
                .quantity(request.getQuantity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(PromotionStatus.ACTIVE) // Default to ACTIVE immediately
                .build();

        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Promotion updatePromotion(Long promotionId, PromotionUpdateDTO request) {
        Shop shop = authHelper.getCurrentSellerShop();
        
        Promotion promotion = promotionRepository.findByIdAndShop_Id(promotionId, shop.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi hoặc bạn không có quyền cập nhật"));

        boolean hasClaims = promotion.getUsedQuantity() > 0;

        // "Safe" fields that can always be updated
        if (request.getName() != null) {
            promotion.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getEndDate() != null) {
            if (!request.getEndDate().isAfter(promotion.getEndDate())) {
                throw new IllegalArgumentException("Không thể rút ngắn ngày kết thúc của khuyến mãi đang tồn tại");
            }
            promotion.setEndDate(request.getEndDate());
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() < promotion.getQuantity()) {
                throw new IllegalArgumentException("Không thể giảm số lượng tổng của khuyến mãi đang tồn tại");
            }
            promotion.setQuantity(request.getQuantity());
        }

        // "Sensitive" fields that can only be updated if no one has claimed it yet
        if (!hasClaims) {
            if (request.getDiscountValue() != null) promotion.setDiscountValue(request.getDiscountValue());
            if (request.getMaxDiscountAmount() != null) promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
            if (request.getMinOrderValue() != null) promotion.setMinOrderValue(request.getMinOrderValue());
            if (request.getMinOrderItems() != null) promotion.setMinOrderItems(request.getMinOrderItems());
            
            // Re-validate percentage limits if discount value changed
            if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
                if (promotion.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                    throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100%");
                }
            }
        } else {
            // Check if user is trying to update sensitive fields when claims exist
            boolean tryingToUpdateSensitive = 
                request.getDiscountValue() != null || 
                request.getMaxDiscountAmount() != null || 
                request.getMinOrderValue() != null || 
                request.getMinOrderItems() != null;
                
            if (tryingToUpdateSensitive) {
                throw new IllegalStateException("Không thể sửa đổi luật giảm giá vì khuyến mãi này đã được người dùng nhận");
            }
        }

        return promotionRepository.save(promotion);
    }

    @Override
    public Page<Promotion> getPromotionsByShop(String keyword, LocalDateTime fromDate, LocalDateTime toDate, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Shop shop = authHelper.getCurrentSellerShop();
        Specification<Promotion> spec = PromotionSpecification.buildFilter(shop.getId(), keyword, fromDate, toDate, minPrice, maxPrice);
        return promotionRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Override
    public Promotion getPromotionDetail(Long promotionId) {
        Shop shop = authHelper.getCurrentSellerShop();
        return promotionRepository.findByIdAndShop_Id(promotionId, shop.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi hoặc bạn không có quyền xem"));
    }

    @Override
    @Transactional
    public Promotion changePromotionStatus(Long promotionId, PromotionStatus newStatus) {
        Shop shop = authHelper.getCurrentSellerShop();
        
        Promotion promotion = promotionRepository.findByIdAndShop_Id(promotionId, shop.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khuyến mãi hoặc bạn không có quyền cập nhật"));

        PromotionStatus currentStatus = promotion.getStatus();
        
        if (currentStatus == PromotionStatus.ACTIVE && newStatus == PromotionStatus.PAUSED) {
            promotion.setStatus(PromotionStatus.PAUSED);
        } else if (currentStatus == PromotionStatus.PAUSED && newStatus == PromotionStatus.ACTIVE) {
            promotion.setStatus(PromotionStatus.ACTIVE);
        } else {
            throw new IllegalStateException("Chuyển đổi trạng thái từ " + currentStatus + " sang " + newStatus + " không hợp lệ. Chỉ cho phép ACTIVE <-> PAUSED.");
        }

        return promotionRepository.save(promotion);
    }
}
