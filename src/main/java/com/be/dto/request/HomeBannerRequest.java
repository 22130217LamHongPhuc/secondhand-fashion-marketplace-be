package com.be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomeBannerRequest {
    private String title;
    private String subtitle;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String linkUrl;
    private Integer orderNum;
    private Boolean isActive;
}
