package com.be.repository;

import com.be.entity.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
    List<HomeBanner> findAllByOrderByOrderNumAsc();
    List<HomeBanner> findByIsActiveTrueOrderByOrderNumAsc();
}
