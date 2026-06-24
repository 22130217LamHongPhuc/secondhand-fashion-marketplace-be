package com.be.dto.request.chat;

import jakarta.validation.constraints.NotNull;

public record ChatConversationCreateRequest(
        @NotNull(message = "Shop ID không được để trống")
        Long shopId
) {
}
