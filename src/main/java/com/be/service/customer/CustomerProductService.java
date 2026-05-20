package com.be.service.customer;

import com.be.dto.response.customer.CategoryItemResponse;
import com.be.dto.response.customer.ProductCardResponse;
import com.be.dto.response.customer.ProductDetailResponse;
import com.be.dto.request.customer.ReviewCreateRequest;
import com.be.dto.response.customer.ReviewCreateResponse;
import com.be.dto.response.customer.ShopDetailWithProductsResponse;
import com.be.dto.response.customer.ShopProductPageResponse;
import com.be.dto.response.customer.ShopPageResponse;

import java.util.List;

public interface CustomerProductService {
    List<CategoryItemResponse> getCurrentCategories();

    List<ProductCardResponse> getHotDeals(int limit);

    List<ProductCardResponse> getNewArrivals(int limit);

    List<ProductCardResponse> getFeaturedShopsProducts(int limit);

    ProductDetailResponse getProductDetail(Long id);

    ShopDetailWithProductsResponse getShopDetailWithProducts(Long shopId, int page, int size);

    ReviewCreateResponse createReview(ReviewCreateRequest request);

    // New: load products by category with pagination
    ShopProductPageResponse getProductsByCategory(Long categoryId, int page, int size);

    // New: filter and sort products with pagination
    ShopProductPageResponse filterAndSortProducts(
            String keyword,
            List<Long> categoryIds,
            String condition,
            List<String> brands,
            List<String> origins,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            String sort,
            int page,
            int size
    );

    // Shops listing and search
    ShopPageResponse listShops(int page, int size);

    ShopPageResponse searchShopsByName(String keyword, int page, int size);
}
