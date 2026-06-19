package com.be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampaignRequest {
    @NotBlank(message = "Campaign name is required")
    private String name;

    private String description;

    private String bannerUrl;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean isAutoSave;
}
