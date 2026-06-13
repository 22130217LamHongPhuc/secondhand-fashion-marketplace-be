package com.be.dto.response.seller.mapper;

import com.be.entity.Product;
import com.be.entity.ProductImage;
import com.be.entity.ProductAttribute;
import com.be.entity.ProductTag;
import com.be.common.enums.ProductCondition;
import com.be.dto.response.seller.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SellerProductMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ProductListResponse toListResponse(Product product) {
        if (product == null) return null;

        BigDecimal basePrice = product.getBasePrice();
        BigDecimal salePrice = product.getSalePrice();
        BigDecimal displayPrice = (salePrice != null) ? salePrice : basePrice;

        boolean hasDiscount = salePrice != null && salePrice.compareTo(basePrice) < 0;

        return new ProductListResponse(
            product.getId(),
            product.getName(),
            product.getBrand(),
            resolveConditionLabel(product.getCondition()),
            formatVND(displayPrice),
            formatVND(basePrice),
            hasDiscount,
            product.getStockQuantity() != null ? product.getStockQuantity() : 0,
            resolveDisplayStatus(product.getIsActive(), product.getStockQuantity()),
            resolveThumbnailUrl(product.getImages())
        );
    }

    public static ProductDetailResponse toDetailResponse(Product product) {
        if (product == null) return null;

        List<ProductImageResponse> imageResponses = product.getImages() != null ?
            product.getImages().stream()
                .map(img -> new ProductImageResponse(img.getId(), img.getUrl(), img.getSortOrder() != null ? img.getSortOrder() : 0, img.getIsPrimary() != null ? img.getIsPrimary() : false))
                .toList() : Collections.emptyList();

        List<ProductAttributeResponse> attributeResponses = product.getAttributes() != null ?
            product.getAttributes().stream()
                .map(attr -> new ProductAttributeResponse(attr.getAttrKey(), attr.getAttrValue()))
                .toList() : Collections.emptyList();

        List<String> tags = product.getTags() != null ?
            product.getTags().stream()
                .map(ProductTag::getTag)
                .toList() : Collections.emptyList();

        return new ProductDetailResponse(
            product.getId(),
            product.getCategory() != null ? product.getCategory().getId() : null,
            product.getName(),
            product.getDescription(),
            product.getBrand(),
            product.getOriginCountry(),
            product.getCondition() != null ? product.getCondition().name() : null,
            resolveConditionLabel(product.getCondition()),
            product.getBasePrice(),
            product.getSalePrice(),
            product.getStockQuantity() != null ? product.getStockQuantity() : 0,
            product.getIsActive() != null ? product.getIsActive() : false,
            product.getRatingAvg(),
            product.getTotalReviews() != null ? product.getTotalReviews() : 0,
            product.getCreatedAt() != null ? product.getCreatedAt().format(DATE_FORMATTER) : null,
            product.getUpdatedAt() != null ? product.getUpdatedAt().format(DATE_FORMATTER) : null,
            imageResponses,
            attributeResponses,
            tags
        );
    }

    public static ProductMutationResponse toMutationResponse(Product product) {
        if (product == null) return null;

        return new ProductMutationResponse(
            product.getId(),
            product.getName(),
            resolveDisplayStatus(product.getIsActive(), product.getStockQuantity()),
            resolveThumbnailUrl(product.getImages())
        );
    }

    public static String formatVND(BigDecimal val) {
        if (val == null) return "0đ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(val) + "đ";
    }

    public static String resolveDisplayStatus(Boolean isActive, Integer stockQuantity) {
        boolean active = isActive != null ? isActive : false;
        int stock = stockQuantity != null ? stockQuantity : 0;
        if (!active) {
            return "Đã ẩn";
        }
        return stock > 0 ? "Đang bán" : "Hết hàng";
    }

    public static String resolveConditionLabel(ProductCondition cond) {
        if (cond == null) return "";
        return switch (cond) {
            case NEW -> "Mới";
            case LIKE_NEW -> "Như mới";
            case GOOD -> "Tốt";
            case FAIR -> "Khá";
        };
    }

    public static String resolveThumbnailUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return "";
        }
        return images.stream()
            .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
            .map(ProductImage::getUrl)
            .findFirst()
            .orElse(images.get(0).getUrl());
    }
}
