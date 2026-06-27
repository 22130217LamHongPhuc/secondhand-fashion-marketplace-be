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

        return promotionRepository.findByShop_IdAndStatusAndStartDateBeforeAndEndDateAfter(shopId,
                PromotionStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public UserPromotion claimPromotion(User user, Long promotionId) {
        try {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow();
            UserPromotion userPromotion = UserPromotion.builder()
                    .user(user)
                    .promotion(promotion)
                    .claimedAt(LocalDateTime.now())
                    .build();
            promotion.setUsedQuantity(promotion.getUsedQuantity() + 1);
            userPromotionRepository.save(userPromotion);
            promotionRepository.save(promotion);
            return userPromotion;
        }catch (Exception e) {
            throw new RuntimeException("Loi khi claim ma giam gia");
        }
    }

    @Override
    public Page<UserPromotion> getMyWallet(User user, int page, int size) {
        return userPromotionRepository.findValidAndAvailablePromotionsByUserId(user.getId()
                , PromotionStatus.ACTIVE, LocalDateTime.now(), PageRequest.of(page, size));
    }
}
