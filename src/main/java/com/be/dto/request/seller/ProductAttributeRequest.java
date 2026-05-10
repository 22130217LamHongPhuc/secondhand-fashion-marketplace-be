package com.be.dto.request.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequest(
        @NotBlank(message = "Attribute key is required")
        @Size(max = 100, message = "Attribute key must not exceed 100 characters")
        String attrKey,

        @NotBlank(message = "Attribute value is required")
        @Size(max = 255, message = "Attribute value must not exceed 255 characters")
        String attrValue
) {
}
