package com.be.service.impl;

import com.be.common.enums.ChatMessageType;
import com.be.common.enums.ChatSenderRole;
import com.be.dto.request.chat.ChatConversationCreateRequest;
import com.be.dto.request.chat.ChatMessageSendRequest;
import com.be.dto.response.chat.ChatConversationResponse;
import com.be.dto.response.chat.ChatMessageResponse;
import com.be.entity.ChatConversation;
import com.be.entity.ChatMessage;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.ChatConversationRepository;
import com.be.repository.ChatMessageRepository;
import com.be.repository.ShopRepository;
import com.be.repository.UserRepository;
import com.be.security.JwtTokenProvider;
import com.be.service.ChatService;
import com.be.service.SseEmitterService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private static final String SCOPE_SELLER = "SELLER";

    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SseEmitterService sseEmitterService;

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getConversations(String scope) {
        User currentUser = getCurrentUser();
        if (SCOPE_SELLER.equalsIgnoreCase(scope)) {
            Shop shop = getCurrentSellerShop(currentUser);
            return chatConversationRepository.findShopConversations(shop.getId())
                    .stream()
                    .map(ChatConversationResponse::from)
                    .toList();
        }

        return chatConversationRepository.findCustomerConversations(currentUser.getId())
                .stream()
                .map(ChatConversationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ChatConversationResponse createConversation(ChatConversationCreateRequest request) {
        User customer = getCurrentUser();
        Shop shop = shopRepository.findByIdAndIsActiveTrue(request.shopId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy shop."));

        if (shop.getSeller() != null && shop.getSeller().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Bạn không thể tự nhắn tin với shop của mình.");
        }

        ChatConversation conversation = chatConversationRepository
                .findByShopIdAndCustomerId(shop.getId(), customer.getId())
                .orElseGet(() -> chatConversationRepository.save(ChatConversation.builder()
                        .shop(shop)
                        .customer(customer)
                        .build()));

        return ChatConversationResponse.from(conversation);
    }

    @Override
    @Transactional
    public ChatConversationResponse getConversation(Long conversationId) {
        User currentUser = getCurrentUser();
        ChatConversation conversation = getAccessibleConversation(conversationId, currentUser);
        
        ChatSenderRole userRole = resolveSenderRole(conversation, currentUser);
        boolean changed = false;
        if (userRole == ChatSenderRole.CUSTOMER && nullToZero(conversation.getCustomerUnreadCount()) > 0) {
            conversation.setCustomerUnreadCount(0);
            changed = true;
        } else if (userRole == ChatSenderRole.SELLER && nullToZero(conversation.getSellerUnreadCount()) > 0) {
            conversation.setSellerUnreadCount(0);
            changed = true;
        }
        
        if (changed) {
            chatConversationRepository.save(conversation);
            
            ChatConversationResponse updatedResponse = ChatConversationResponse.from(conversation);
            if (conversation.getCustomer() != null) {
                sseEmitterService.sendEvent("chat", conversation.getCustomer().getId().toString(), "chat-updated", updatedResponse);
            }
            if (conversation.getShop() != null && conversation.getShop().getSeller() != null) {
                sseEmitterService.sendEvent("chat", conversation.getShop().getSeller().getId().toString(), "chat-updated", updatedResponse);
            }
        }

        List<ChatMessageResponse> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();

        return ChatConversationResponse.from(conversation, messages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId) {
        ChatConversation conversation = getAccessibleConversation(conversationId, getCurrentUser());
        return chatMessageRepository.findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, ChatMessageSendRequest request) {
        User currentUser = getCurrentUser();
        ChatConversation conversation = getAccessibleConversation(conversationId, currentUser);
        ChatSenderRole senderRole = resolveSenderRole(conversation, currentUser);
        ChatMessageType messageType = request.messageType() != null ? request.messageType() : ChatMessageType.TEXT;
        String content = StringUtils.hasText(request.content()) ? request.content().trim() : null;
        String imageUrl = StringUtils.hasText(request.imageUrl()) ? request.imageUrl().trim() : null;

        if (messageType == ChatMessageType.TEXT && !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống.");
        }
        if (messageType == ChatMessageType.IMAGE && !StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("Ảnh tin nhắn không được để trống.");
        }

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .sender(currentUser)
                .senderRole(senderRole)
                .messageType(messageType)
                .content(content)
                .imageUrl(imageUrl)
                .build());

        conversation.setLastMessagePreview(buildPreview(message));
        conversation.setLastMessageAt(LocalDateTime.now());
        if (senderRole == ChatSenderRole.CUSTOMER) {
            conversation.setSellerUnreadCount(nullToZero(conversation.getSellerUnreadCount()) + 1);
        } else {
            conversation.setCustomerUnreadCount(nullToZero(conversation.getCustomerUnreadCount()) + 1);
        }
        chatConversationRepository.save(conversation);

        ChatMessageResponse messageResponse = ChatMessageResponse.from(message);
        ChatConversationResponse conversationResponse = ChatConversationResponse.from(conversation);

        if (conversation.getCustomer() != null) {
            sseEmitterService.sendEvent("chat", conversation.getCustomer().getId().toString(), "chat-message", messageResponse);
            sseEmitterService.sendEvent("chat", conversation.getCustomer().getId().toString(), "chat-updated", conversationResponse);
        }
        if (conversation.getShop() != null && conversation.getShop().getSeller() != null) {
            sseEmitterService.sendEvent("chat", conversation.getShop().getSeller().getId().toString(), "chat-message", messageResponse);
            sseEmitterService.sendEvent("chat", conversation.getShop().getSeller().getId().toString(), "chat-updated", conversationResponse);
        }

        return messageResponse;
    }

    private ChatConversation getAccessibleConversation(Long conversationId, User currentUser) {
        ChatConversation conversation = chatConversationRepository.findDetailById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hội thoại."));

        resolveSenderRole(conversation, currentUser);
        return conversation;
    }

    private ChatSenderRole resolveSenderRole(ChatConversation conversation, User currentUser) {
        if (conversation.getCustomer().getId().equals(currentUser.getId())) {
            return ChatSenderRole.CUSTOMER;
        }
        if (conversation.getShop().getSeller() != null
                && conversation.getShop().getSeller().getId().equals(currentUser.getId())) {
            return ChatSenderRole.SELLER;
        }
        throw new IllegalArgumentException("Bạn không có quyền truy cập hội thoại này.");
    }

    private Shop getCurrentSellerShop(User currentUser) {
        return shopRepository.findBySellerId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy shop của seller hiện tại."));
    }

    private User getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    return userRepository.findByEmail(email)
                            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng."));
                }
            }
        } catch (Exception ignored) {
            // Fall through to a clear error below.
        }
        throw new IllegalArgumentException("Bạn cần đăng nhập để sử dụng chat.");
    }

    private String buildPreview(ChatMessage message) {
        if (message.getMessageType() == ChatMessageType.IMAGE) {
            return "[Hình ảnh]";
        }
        String content = message.getContent();
        if (content == null || content.length() <= 500) {
            return content;
        }
        return content.substring(0, 500);
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
