package com.be.dto.response.chat;

import com.be.common.enums.ChatConversationStatus;
import com.be.entity.ChatConversation;

import java.time.LocalDateTime;
import java.util.List;

public record ChatConversationResponse(
        Long id,
        Long shopId,
        String shopName,
        String shopAvatarUrl,
        Long customerId,
        String customerName,
        String customerAvatarUrl,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        Integer customerUnreadCount,
        Integer sellerUnreadCount,
        ChatConversationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ChatMessageResponse> messages
) {
    public static ChatConversationResponse from(ChatConversation conversation) {
        return from(conversation, null);
    }

    public static ChatConversationResponse from(
            ChatConversation conversation,
            List<ChatMessageResponse> messages
    ) {
        return new ChatConversationResponse(
                conversation.getId(),
                conversation.getShop().getId(),
                conversation.getShop().getName(),
                conversation.getShop().getAvatarUrl(),
                conversation.getCustomer().getId(),
                conversation.getCustomer().getFullName(),
                conversation.getCustomer().getAvatarUrl(),
                conversation.getLastMessagePreview(),
                conversation.getLastMessageAt(),
                conversation.getCustomerUnreadCount(),
                conversation.getSellerUnreadCount(),
                conversation.getStatus(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages
        );
    }
}
