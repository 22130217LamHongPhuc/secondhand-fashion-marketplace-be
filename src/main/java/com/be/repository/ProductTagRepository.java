package com.be.repository;

import com.be.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, String> {
    List<ProductTag> findByProductId(String productId);

    long countByTag(String tag);
}

