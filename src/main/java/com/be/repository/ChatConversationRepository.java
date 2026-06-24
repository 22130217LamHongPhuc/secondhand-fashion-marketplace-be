package com.be.repository;

import com.be.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByShopIdAndCustomerId(Long shopId, Long customerId);

    @Query("""
            SELECT c FROM ChatConversation c
            JOIN FETCH c.shop s
            JOIN FETCH s.seller
            JOIN FETCH c.customer
            WHERE c.customer.id = :customerId
            ORDER BY COALESCE(c.lastMessageAt, c.updatedAt, c.createdAt) DESC
            """)
    List<ChatConversation> findCustomerConversations(Long customerId);

    @Query("""
            SELECT c FROM ChatConversation c
            JOIN FETCH c.shop s
            JOIN FETCH s.seller
            JOIN FETCH c.customer
            WHERE c.shop.id = :shopId
            ORDER BY COALESCE(c.lastMessageAt, c.updatedAt, c.createdAt) DESC
            """)
    List<ChatConversation> findShopConversations(Long shopId);

    @Query("""
            SELECT c FROM ChatConversation c
            JOIN FETCH c.shop s
            JOIN FETCH s.seller
            JOIN FETCH c.customer
            WHERE c.id = :id
            """)
    Optional<ChatConversation> findDetailById(Long id);
}
