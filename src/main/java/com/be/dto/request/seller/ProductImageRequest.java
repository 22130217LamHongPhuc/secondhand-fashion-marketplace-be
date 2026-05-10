package com.be.dto.request.seller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductImageRequest(
        @NotBlank(message = "Image url is required")
        String url,

        @Min(value = 0, message = "Image sort order must be greater than or equal to 0")
        Integer sortOrder,

        Boolean isPrimary
) {
}
