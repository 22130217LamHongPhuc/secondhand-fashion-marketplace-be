package com.be.repository;

import com.be.common.enums.CampaignProductStatus;
import com.be.entity.CampaignProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignProductRepository extends JpaRepository<CampaignProduct, Long> {
    List<CampaignProduct> findByCampaignId(Long campaignId);
    
    List<CampaignProduct> findByCampaignIdAndStatus(Long campaignId, CampaignProductStatus status);
    
    Optional<CampaignProduct> findByCampaignIdAndProductId(Long campaignId, Long productId);
    
    List<CampaignProduct> findByProductIdAndStatus(Long productId, CampaignProductStatus status);
}
