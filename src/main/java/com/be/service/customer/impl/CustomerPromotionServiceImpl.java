package com.be.service.customer.impl;

import com.be.constant.PromotionStatus;
import com.be.entity.Promotion;
import com.be.entity.User;
import com.be.entity.UserPromotion;
import com.be.repository.PromotionRepository;
import com.be.repository.UserPromotionRepository;
import com.be.service.customer.CustomerPromotionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CustomerPromotionServiceImpl implements CustomerPromotionService {
    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    @Override
    public Page<Promotion> getAvailablePromotions(Long shopId, int page, int size) {
        return promotionRepository.findAvailablePromotions(shopId,
                PromotionStatus.ACTIVE, LocalDateTime.now(), PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public UserPromotion claimPromotion(User user, Long promotionId) {
        if (user == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Yêu cầu đăng nhập để lưu mã giảm giá");
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chương trình khuyến mãi"));

        // 1. Kiểm tra trạng thái hoạt động
        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new IllegalStateException("Khuyến mãi hiện không hoạt động");
        }

        // 2. Kiểm tra thời gian hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartDate())) {
            throw new IllegalStateException("Khuyến mãi chưa bắt đầu");
        }
        if (now.isAfter(promotion.getEndDate())) {
            throw new IllegalStateException("Khuyến mãi đã hết hạn");
        }

        // 3. Kiểm tra số lượng còn lại
        if (promotion.getQuantity() != null && promotion.getUsedQuantity() != null
                && promotion.getUsedQuantity() >= promotion.getQuantity()) {
            throw new IllegalStateException("Mã giảm giá đã hết lượt sử dụng");
        }

        // 4. Kiểm tra xem người dùng đã claim chưa
        boolean alreadyClaimed = userPromotionRepository.existsByUser_IdAndPromotion_Id(user.getId(), promotionId);
        if (alreadyClaimed) {
            throw new IllegalStateException("Bạn đã lưu mã giảm giá này rồi");
        }

        UserPromotion userPromotion = UserPromotion.builder()
                .user(user)
                .promotion(promotion)
                .usageCount(0)
                .claimedAt(LocalDateTime.now())
                .build();

        // Tăng usedQuantity của promotion
        int newUsedQuantity = promotion.getUsedQuantity() == null ? 1 : promotion.getUsedQuantity() + 1;
        promotion.setUsedQuantity(newUsedQuantity);

        promotionRepository.save(promotion);
        return userPromotionRepository.save(userPromotion);
    }

    @Override
    public Page<UserPromotion> getMyWallet(User user, int page, int size) {
        if (user == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Yêu cầu đăng nhập để xem ví voucher");
        }
        return userPromotionRepository.findByUser_Id(user.getId(), PageRequest.of(page, size));
    }
}
