package com.be.dto.request.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotBlank(message = "Comment content is required")
        String content,

        Long parentId // Optional - for nested comments (reply to comment)
) {
}

