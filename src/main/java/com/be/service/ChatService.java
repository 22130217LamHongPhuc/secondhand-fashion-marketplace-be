package com.be.service;

import com.be.dto.request.chat.ChatConversationCreateRequest;
import com.be.dto.request.chat.ChatMessageSendRequest;
import com.be.dto.response.chat.ChatConversationResponse;
import com.be.dto.response.chat.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    List<ChatConversationResponse> getConversations(String scope);

    ChatConversationResponse createConversation(ChatConversationCreateRequest request);

    ChatConversationResponse getConversation(Long conversationId);

    List<ChatMessageResponse> getMessages(Long conversationId);

    ChatMessageResponse sendMessage(Long conversationId, ChatMessageSendRequest request);
}
