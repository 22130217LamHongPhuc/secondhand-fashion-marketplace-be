package com.be.dto.request.seller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ProductImageRequest(
        @NotNull(message = "Image file is required")
        MultipartFile file,

        @Min(value = 0, message = "Image sort order must be greater than or equal to 0")
        Integer sortOrder,

        Boolean isPrimary
) {
}
