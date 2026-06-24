package com.be.dto.request.chat;

import com.be.common.enums.ChatMessageType;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(
        @Size(max = 5000, message = "Nội dung tin nhắn tối đa 5000 ký tự")
        String content,
        String imageUrl,
        ChatMessageType messageType
) {
}
