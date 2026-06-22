package com.be.dto.response;

import com.be.common.enums.CampaignProductStatus;
import com.be.entity.CampaignProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignProductResponse {
    private Long id;
    private Long campaignId;
    private String campaignName;
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal campaignPrice;
    private CampaignProductStatus status;
    private String shopName;
    private LocalDateTime createdAt;

    public static CampaignProductResponse fromEntity(CampaignProduct cp) {
        if (cp == null) {
            return null;
        }

        return CampaignProductResponse.builder()
                .id(cp.getId())
                .campaignId(cp.getCampaign() != null ? cp.getCampaign().getId() : null)
                .campaignName(cp.getCampaign() != null ? cp.getCampaign().getName() : null)
                .productId(cp.getProduct() != null ? cp.getProduct().getId() : null)
                .productName(cp.getProduct() != null ? cp.getProduct().getName() : null)
                .originalPrice(cp.getProduct() != null ? cp.getProduct().getBasePrice() : null)
                .campaignPrice(cp.getCampaignPrice())
                .status(cp.getStatus())
                .shopName((cp.getProduct() != null && cp.getProduct().getShop() != null) ? cp.getProduct().getShop().getName() : null)
                .createdAt(cp.getCreatedAt())
                .build();
    }
}
