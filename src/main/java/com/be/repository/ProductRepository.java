package com.be.repository;

import com.be.entity.Product;
import com.be.common.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.id > :lastId AND p.isActive = true ORDER BY p.id ASC")
    Page<Product> getListByPage(@Param("lastId") long lastId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id > :lastId AND p.isActive = :isActive ORDER BY p.id ASC")
    Page<Product> getListByStatus(
            @Param("isActive") Boolean isActive,
            @Param("lastId") long lastId,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id IN :ids ORDER BY p.id ASC")
    List<Product> findAllWithImagesByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.attributes
            LEFT JOIN FETCH p.tags
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithDetails(@Param("id") long id);

    Page<Product> findByShopIdAndIsActiveTrue(Long shopId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    List<Product> findTop8ByCategoryIdAndIsActiveTrueAndIdNotOrderByCreatedAtDesc(Long categoryId, Long id);

    Page<Product> findByConditionAndIsActiveTrue(ProductCondition condition, Pageable pageable);

    Page<Product> findByIsActiveTrueAndStockQuantityGreaterThan(int stock, Pageable pageable);

    @Query(value = "SELECT * FROM products WHERE MATCH(name, brand) AGAINST(?1 IN BOOLEAN MODE) AND is_active = true", nativeQuery = true)
    Page<Product> searchByKeyword(String keyword, Pageable pageable);

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
