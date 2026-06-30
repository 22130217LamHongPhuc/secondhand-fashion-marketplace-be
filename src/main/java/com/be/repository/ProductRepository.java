package com.be.repository;

import com.be.entity.Product;
import com.be.common.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.QueryHint;
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

    long countByShopId(Long shopId);

    @QueryHints(value = @QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId ORDER BY p.createdAt DESC")
    java.util.stream.Stream<Product> streamAllByShopIdOrderByCreatedAtDesc(@Param("shopId") Long shopId);

    List<Product> findAllByShopIdOrderByCreatedAtDesc(Long shopId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.shop.id = :shopId
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isActive IS NULL OR p.isActive = :isActive)
            ORDER BY p.createdAt DESC
            """)
    Page<Product> searchShopProducts(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.shop LEFT JOIN FETCH p.images ORDER BY p.id DESC")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id IN :ids ORDER BY p.id ASC")
    List<Product> findAllWithImagesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.shop LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") long id);

    Page<Product> findByShopIdAndIsActiveTrueAndIsApprovedTrue(Long shopId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrueAndIsApprovedTrue(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdInAndIsActiveTrueAndIsApprovedTrue(List<Long> categoryIds, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrueAndIsApprovedTrue(Long id);

    @Query(value = "SELECT * FROM products WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    List<Product> findTop8ByCategoryIdAndIsActiveTrueAndIsApprovedTrueAndIdNotOrderByCreatedAtDesc(Long categoryId, Long id);

    Page<Product> findByConditionAndIsActiveTrueAndIsApprovedTrue(ProductCondition condition, Pageable pageable);

    Page<Product> findByIsActiveTrueAndIsApprovedTrueAndStockQuantityGreaterThan(int stock, Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true AND p.isApproved = true
              AND p.stockQuantity > 0
              AND p.salePrice IS NOT NULL
              AND p.basePrice > p.salePrice
            ORDER BY (p.basePrice - p.salePrice) DESC, p.createdAt DESC
            """)
    List<Product> findHotDeals(Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true AND p.isApproved = true
              AND p.stockQuantity > 0
            ORDER BY p.createdAt DESC
            """)
    List<Product> findNewArrivals(Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.isActive = true AND p.isApproved = true
              AND p.stockQuantity > 0
              AND p.shop.id IN :shopIds
            ORDER BY p.createdAt DESC
            """)
    List<Product> findByFeaturedShopIds(@Param("shopIds") List<Long> shopIds, Pageable pageable);
}
