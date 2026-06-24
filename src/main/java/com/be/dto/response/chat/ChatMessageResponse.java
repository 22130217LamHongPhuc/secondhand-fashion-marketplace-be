package com.be.dto.response.chat;

import com.be.common.enums.ChatMessageType;
import com.be.common.enums.ChatSenderRole;
import com.be.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        ChatSenderRole senderRole,
        ChatMessageType messageType,
        String content,
        String imageUrl,
        Boolean isDeleted,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getSenderRole(),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                message.getIsDeleted(),
                message.getCreatedAt()
        );
    }
}
