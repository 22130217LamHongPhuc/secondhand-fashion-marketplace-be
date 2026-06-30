package com.be.repository;

import com.be.constant.PromotionStatus;
import com.be.entity.UserPromotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {

    // Kiểm tra xem user này đã claim mã này chưa
    boolean existsByUser_IdAndPromotion_Id(Long userId, Long promotionId);

    // Lấy danh sách các mã nằm trong "Ví Voucher" của người mua
    Page<UserPromotion> findByUser_Id(Long userId, Pageable pageable);

    @Query(
            value = "SELECT up FROM UserPromotion up " +
                    "JOIN FETCH up.promotion p " +
                    "WHERE up.user.id = :userId " +
                    "AND p.status = :status " +
                    "AND p.startDate <= :now " +
                    "AND p.endDate > :now " +
                    "AND p.usedQuantity < p.quantity",
            countQuery = "SELECT COUNT(up) FROM UserPromotion up " +
                    "JOIN up.promotion p " + // Bỏ chữ FETCH ở đây
                    "WHERE up.user.id = :userId " +
                    "AND p.status = :status " +
                    "AND p.startDate <= :now " +
                    "AND p.endDate > :now " +
                    "AND p.usedQuantity < p.quantity"
    )
    Page<UserPromotion> findValidAndAvailablePromotionsByUserId(
            @Param("userId") Long userId,
            @Param("status") PromotionStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable // Thêm tham số này vào cuối
    );

    @Query("SELECT up FROM UserPromotion up JOIN FETCH up.promotion p WHERE up.user.id = :userId AND p.code = :code")
    java.util.List<UserPromotion> findByUserIdAndPromotionCode(
            @Param("userId") Long userId,
            @Param("code") String code);
}
