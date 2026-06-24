package com.be.entity;

import com.be.common.enums.ChatMessageType;
import com.be.common.enums.ChatSenderRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_conversation", columnList = "conversation_id,created_at"),
    @Index(name = "idx_chat_messages_sender", columnList = "sender_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"conversation", "sender"})
@ToString(exclude = {"conversation", "sender"})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private ChatSenderRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String content;

    @Column(name = "image_url", length = 1000, nullable = true)
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
