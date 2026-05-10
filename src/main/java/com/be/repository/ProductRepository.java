package com.be.repository;

import com.be.entity.Product;
import com.be.common.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByShopIdAndIsActiveTrue(Long shopId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByConditionAndIsActiveTrue(ProductCondition condition, Pageable pageable);

    Page<Product> findByIsActiveTrueAndStockQuantityGreaterThan(int stock, Pageable pageable);

    @Query(value = "SELECT * FROM products WHERE MATCH(name, brand) AGAINST(?1 IN BOOLEAN MODE) AND is_active = true", nativeQuery = true)
    Page<Product> searchByKeyword(String keyword, Pageable pageable);
}

