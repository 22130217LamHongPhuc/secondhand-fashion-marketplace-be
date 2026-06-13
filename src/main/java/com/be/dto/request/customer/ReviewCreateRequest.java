package com.be.dto.request.customer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(
        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "Product id is required")
        Long productId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        @Size(max = 2000, message = "Comment must not exceed 2000 characters")
        String comment,

        @Size(max = 5, message = "Review can have at most 5 images")
        List<MultipartFile> images
) {
}
