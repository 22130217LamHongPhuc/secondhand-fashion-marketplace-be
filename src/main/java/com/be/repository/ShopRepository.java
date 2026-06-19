package com.be.repository;

import com.be.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByIdAndIsActiveTrue(Long id);

    Optional<Shop> findBySlug(String slug);

    Optional<Shop> findBySellerId(Long sellerId);

    boolean existsBySellerId(Long sellerId);

    boolean existsBySlug(String slug);

    long countByIsVerified(Boolean isVerified);

    org.springframework.data.domain.Page<Shop> findByIsActiveTrue(org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Shop> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, org.springframework.data.domain.Pageable pageable);
}

