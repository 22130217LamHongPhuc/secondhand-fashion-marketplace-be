package com.be.repository;

import com.be.entity.RevenueSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RevenueSnapshotRepository extends JpaRepository<RevenueSnapshot, Long> {
    Page<RevenueSnapshot> findByShopId(Long shopId, Pageable pageable);

    Optional<RevenueSnapshot> findByShopIdAndSnapshotDate(Long shopId, LocalDate snapshotDate);

    Page<RevenueSnapshot> findByShopIdAndSnapshotDateBetween(Long shopId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}

