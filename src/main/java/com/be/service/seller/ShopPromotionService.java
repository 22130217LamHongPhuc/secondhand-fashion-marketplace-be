package com.be.service.seller;

import com.be.constant.PromotionStatus;
import com.be.dto.request.seller.PromotionCreateDTO;
import com.be.dto.request.seller.PromotionUpdateDTO;
import com.be.entity.Promotion;
import org.springframework.data.domain.Page;

public interface ShopPromotionService {
    
    // Tạo mã khuyến mãi mới
    Promotion createPromotion(PromotionCreateDTO request);

    // Cập nhật mã khuyến mãi
    Promotion updatePromotion(Long promotionId, PromotionUpdateDTO request);

    // Lấy danh sách mã để hiển thị trên dashboard của shop
    Page<Promotion> getPromotionsByShop(String keyword, int page, int size);

    // Tạm dừng (PAUSED) hoặc Kích hoạt lại (ACTIVE) một mã giảm giá
    Promotion changePromotionStatus(Long promotionId, PromotionStatus newStatus);
}
