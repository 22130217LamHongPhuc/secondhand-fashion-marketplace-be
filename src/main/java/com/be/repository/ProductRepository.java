package com.be.repository;

import com.be.entity.Product;
import com.be.common.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isActive IS NULL OR p.isActive = :isActive)
            ORDER BY p.createdAt DESC
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id IN :ids ORDER BY p.id ASC")
    List<Product> findAllWithImagesByIds(@Param("ids") List<Long> ids);

    @Query(value = "SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") long id);

    Page<Product> findByShopIdAndIsActiveTrue(Long shopId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    List<Product> findTop8ByCategoryIdAndIsActiveTrueAndIdNotOrderByCreatedAtDesc(Long categoryId, Long id);

    Page<Product> findByConditionAndIsActiveTrue(ProductCondition condition, Pageable pageable);

    Page<Product> findByIsActiveTrueAndStockQuantityGreaterThan(int stock, Pageable pageable);



    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true
              AND p.stockQuantity > 0
              AND p.salePrice IS NOT NULL
              AND p.basePrice > p.salePrice
            ORDER BY (p.basePrice - p.salePrice) DESC, p.createdAt DESC
            """)
    List<Product> findHotDeals(Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true
              AND p.stockQuantity > 0
            ORDER BY p.createdAt DESC
            """)
    List<Product> findNewArrivals(Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true
              AND p.stockQuantity > 0
              AND p.shop.id IN :shopIds
            ORDER BY p.createdAt DESC
            """)
    List<Product> findByFeaturedShopIds(@Param("shopIds") List<Long> shopIds, Pageable pageable);
}
