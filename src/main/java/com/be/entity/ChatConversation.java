package com.be.entity;

import com.be.common.enums.ChatConversationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_conversations", indexes = {
    @Index(name = "idx_chat_customer", columnList = "customer_id,last_message_at"),
    @Index(name = "idx_chat_shop", columnList = "shop_id,last_message_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_chat_shop_customer", columnNames = {"shop_id", "customer_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"shop", "customer", "messages"})
@ToString(exclude = {"shop", "customer", "messages"})
public class ChatConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private User customer;

    @Column(name = "last_message_preview", length = 500, nullable = true)
    private String lastMessagePreview;

    @Column(name = "last_message_at", nullable = true)
    private LocalDateTime lastMessageAt;

    @Column(name = "customer_unread_count", nullable = false)
    @Builder.Default
    private Integer customerUnreadCount = 0;

    @Column(name = "seller_unread_count", nullable = false)
    @Builder.Default
    private Integer sellerUnreadCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatConversationStatus status = ChatConversationStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatMessage> messages;
}
