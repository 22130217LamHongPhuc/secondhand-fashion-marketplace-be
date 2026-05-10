package com.be.dto.request.seller;

import com.be.common.enums.ProductCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateRequest(
        Long categoryId,

        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @Size(max = 5000, message = "Product description must not exceed 5000 characters")
        String description,

        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @Size(max = 100, message = "Origin country must not exceed 100 characters")
        String originCountry,

        ProductCondition condition,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
        BigDecimal basePrice,

        @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
        BigDecimal salePrice,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        Integer stockQuantity,

        @Size(max = 20, message = "Product can have at most 20 images")
        List<@Valid ProductImageRequest> images,

        @Size(max = 30, message = "Product can have at most 30 attributes")
        List<@Valid ProductAttributeRequest> attributes,

        @Size(max = 20, message = "Product can have at most 20 tags")
        List<@NotBlank(message = "Tag must not be blank") @Size(max = 100, message = "Tag must not exceed 100 characters") String> tags
) {
    @AssertTrue(message = "Sale price must be less than or equal to base price")
    public boolean isSalePriceValid() {
        return salePrice == null || basePrice == null || salePrice.compareTo(basePrice) <= 0;
    }
}
