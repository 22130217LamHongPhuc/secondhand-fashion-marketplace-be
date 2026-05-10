package com.be.repository;

import com.be.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findBySlug(String slug);

    Optional<Shop> findBySellerId(Long sellerId);

    long countByIsVerified(Boolean isVerified);
}

