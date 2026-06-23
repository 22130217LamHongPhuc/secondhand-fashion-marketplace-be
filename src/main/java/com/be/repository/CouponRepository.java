package com.be.repository;

import com.be.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    @Query(value = "SELECT * FROM coupons WHERE code = :code FOR UPDATE", nativeQuery = true)
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
    
    List<Coupon> findByShopId(Long shopId);
    
    List<Coupon> findByIsActiveTrue();
}
