package com.be.service.customer;

import com.be.dto.response.customer.CategoryItemResponse;
import com.be.dto.response.customer.ProductCardResponse;
import com.be.dto.response.customer.ProductDetailResponse;
import com.be.dto.request.customer.ReviewCreateRequest;
import com.be.dto.response.customer.ReviewCreateResponse;
import com.be.dto.response.customer.ShopDetailWithProductsResponse;

import java.util.List;

public interface CustomerProductService {
    List<CategoryItemResponse> getCurrentCategories();

    List<ProductCardResponse> getHotDeals(int limit);

    List<ProductCardResponse> getNewArrivals(int limit);

    List<ProductCardResponse> getFeaturedShopsProducts(int limit);

    ProductDetailResponse getProductDetail(Long id);

    ShopDetailWithProductsResponse getShopDetailWithProducts(Long shopId, int page, int size);

    ReviewCreateResponse createReview(ReviewCreateRequest request);
}

