package com.be.repository;

import com.be.constant.PromotionStatus;
import com.be.entity.Promotion;
import com.be.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Lấy danh sách mã của một shop (dành cho chủ shop quản lý)
    Page<Promotion> findByShop_Id(Long shopId, Pageable pageable);
    
    // Tìm kiếm mã promotion theo keyword chứa trong code (không phân biệt hoa thường)
    Page<Promotion> findByShop_IdAndCodeContainingIgnoreCase(Long shopId, String code, Pageable pageable);
    
    // Lấy danh sách mã đang hoạt động và còn hạn của một shop (dành cho người mua xem)
    Page<Promotion> findByShop_IdAndStatusAndStartDateBeforeAndEndDateAfter(
            Long shopId, PromotionStatus status, LocalDateTime currentDate1, LocalDateTime currentDate2, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            "SELECT p FROM Promotion p WHERE p.shop.id = :shopId " +
            "AND p.status = :status " +
            "AND p.startDate <= :now " +
            "AND p.endDate > :now " +
            "AND p.usedQuantity < p.quantity"
    )
    Page<Promotion> findAvailablePromotions(
            @org.springframework.data.repository.query.Param("shopId") Long shopId,
            @org.springframework.data.repository.query.Param("status") PromotionStatus status,
            @org.springframework.data.repository.query.Param("now") LocalDateTime now,
            Pageable pageable);
            
    // Kiểm tra mã code đã tồn tại trong shop chưa để tránh trùng lặp khi tạo mới
    boolean existsByShop_IdAndCode(Long shopId, String code);

    // Tìm promotion thuộc shop cụ thể (dùng cho ownership check)
    java.util.Optional<Promotion> findByIdAndShop_Id(Long id, Long shopId);

    // Tìm tất cả promotion có code cụ thể (dành cho kiểm tra giỏ hàng/thanh toán)
    java.util.List<Promotion> findByCode(String code);
}
