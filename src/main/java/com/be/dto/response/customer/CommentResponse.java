package com.be.dto.response.customer;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long productId,
        Long userId,
        String userName,
        String userAvatar,
        String content,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

