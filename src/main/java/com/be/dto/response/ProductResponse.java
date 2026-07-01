package com.be.dto.response;

import com.be.common.enums.ProductCondition;
import com.be.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private String originCountry;
    private ProductCondition condition;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer stock;
    private BigDecimal ratingAvg;
    private Integer totalReviews;
    private Boolean isActive;
    private Boolean isApproved;
    private String status;
    private String category;
    private String categoryName;
    private String sellerName;
    private String sellerMeta;
    private String sku;
    private String image;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ShopInfo shop;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShopInfo {
        private Long id;
        private String name;
    }

    public static ProductResponse fromEntity(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Chưa phân loại";
        String sellerName = product.getShop() != null ? product.getShop().getName() : "Cửa hàng";
        BigDecimal salePrice = product.getSalePrice();
        BigDecimal price = salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0
                ? salePrice
                : product.getBasePrice();
        String status = "pending";
        if (Boolean.TRUE.equals(product.getIsApproved())) {
            status = Boolean.TRUE.equals(product.getIsActive()) ? "selling" : "locked";
        }
        
        List<String> imageUrls = product.getImages() != null ? 
                product.getImages().stream()
                        .map(img -> img.getUrl())
                        .collect(Collectors.toList()) : List.of();
        
        String primaryImage = !imageUrls.isEmpty() ? imageUrls.get(0) : 
                "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=300&q=80";

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .originCountry(product.getOriginCountry())
                .condition(product.getCondition())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .price(price)
                .stockQuantity(product.getStockQuantity())
                .stock(product.getStockQuantity())
                .ratingAvg(product.getRatingAvg())
                .totalReviews(product.getTotalReviews())
                .isActive(product.getIsActive())
                .isApproved(product.getIsApproved())
                .status(status)
                .category(categoryName)
                .categoryName(categoryName)
                .sellerName(sellerName)
                .sellerMeta(product.getCondition() != null ? product.getCondition().name() : "GOOD")
                .sku("SKU-" + String.format("%03d", product.getId() % 1000))
                .image(primaryImage)
                .images(imageUrls)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .shop(product.getShop() != null ? ShopInfo.builder()
                        .id(product.getShop().getId())
                        .name(product.getShop().getName())
                        .build() : null)
                .build();
    }

}
