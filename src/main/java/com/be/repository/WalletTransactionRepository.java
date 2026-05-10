package com.be.repository;

import com.be.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);

    Page<WalletTransaction> findByOrderId(Long orderId, Pageable pageable);
}

