package com.be.repository;

import com.be.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByProductIdAndIsVisibleTrue(Long productId, Pageable pageable);

    Page<Comment> findByProductIdAndParentIsNullAndIsVisibleTrue(Long productId, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);
    
    List<Comment> findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(Long productId);
}

