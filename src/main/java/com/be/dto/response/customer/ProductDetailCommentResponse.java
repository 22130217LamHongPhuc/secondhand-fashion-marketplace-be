    package com.be.dto.response.customer;

import java.time.LocalDateTime;

public record ProductDetailCommentResponse(
        Long id,
        Long commenterId,
        String commenterName,
        String commenterAvatarUrl,
        String content,
        LocalDateTime createdAt
) {
}

