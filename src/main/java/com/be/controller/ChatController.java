package com.be.controller;

import com.be.dto.request.chat.ChatConversationCreateRequest;
import com.be.dto.request.chat.ChatMessageSendRequest;
import com.be.dto.response.ApiResponse;
import com.be.dto.response.chat.ChatConversationResponse;
import com.be.dto.response.chat.ChatMessageResponse;
import com.be.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getConversations(
            @RequestParam(defaultValue = "CUSTOMER") String scope
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversations(scope),
                "Lấy danh sách hội thoại thành công"
        ));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> createConversation(
            @Valid @RequestBody ChatConversationCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.createConversation(request),
                "Tạo hội thoại thành công"
        ));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> getConversation(
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversation(conversationId),
                "Lấy hội thoại thành công"
        ));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getMessages(conversationId),
                "Lấy tin nhắn thành công"
        ));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageSendRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.sendMessage(conversationId, request),
                "Gửi tin nhắn thành công"
        ));
    }
}
