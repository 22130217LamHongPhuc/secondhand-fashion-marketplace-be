package com.be.repository;

import com.be.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByProductIdAndIsVisibleTrue(String productId, Pageable pageable);

    Page<Comment> findByProductIdAndParentIsNullAndIsVisibleTrue(String productId, Pageable pageable);

    Page<Comment> findByUserId(String userId, Pageable pageable);
}

