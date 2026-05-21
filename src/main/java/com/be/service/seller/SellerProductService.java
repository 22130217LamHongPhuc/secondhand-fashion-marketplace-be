package com.be.service.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.seller.ProductListResponse;
import com.be.dto.response.seller.ProductDetailResponse;
import com.be.dto.response.seller.ProductMutationResponse;
import org.springframework.data.domain.Page;

public interface SellerProductService {
    Page<ProductListResponse> searchProducts(String keyword, Boolean isActive, int page);
    ProductDetailResponse getDetails(long id);
    ProductMutationResponse createProduct(ProductCreateRequest request);
    ProductMutationResponse updateProduct(long id, ProductUpdateRequest request);
    void deleteProduct(long id);
}
