package com.be.dto.response;

import com.be.entity.Campaign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isAutoSave;
    private LocalDateTime createdAt;

    public static CampaignResponse fromEntity(Campaign campaign) {
        if (campaign == null) {
            return null;
        }

        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .bannerUrl(campaign.getBannerUrl())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .isActive(campaign.getIsActive())
                .isAutoSave(campaign.getIsAutoSave())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}
