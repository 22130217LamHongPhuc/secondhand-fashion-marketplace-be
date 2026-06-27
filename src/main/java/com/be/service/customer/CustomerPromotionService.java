package com.be.service.customer;

import com.be.entity.Promotion;
import com.be.entity.User;
import com.be.entity.UserPromotion;
import org.springframework.data.domain.Page;

public interface CustomerPromotionService {

    // Lấy danh sách các mã hợp lệ của một shop để hiển thị khi user vào xem shop
    Page<Promotion> getAvailablePromotions(Long shopId, int page, int size);

    // Bấm thu thập mã giảm giá (Lưu vào ví)
    UserPromotion claimPromotion(User user, Long promotionId);

    // Lấy danh sách mã mà user đang sở hữu trong ví
    Page<UserPromotion> getMyWallet(User user, int page, int size);
}
