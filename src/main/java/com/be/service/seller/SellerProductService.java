package com.be.service.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.seller.ProductListResponse;
import com.be.dto.response.seller.ProductDetailResponse;
import com.be.dto.response.seller.ProductMutationResponse;
import org.springframework.data.domain.Page;

public interface SellerProductService {
    Page<ProductListResponse> getListByPage(long lastId, int page);
    ProductDetailResponse getDetails(long id);
    Page<ProductListResponse> getListByStatus(Boolean isActive, long lastId, int page);
    ProductMutationResponse createProduct(ProductCreateRequest request);
    ProductMutationResponse updateProduct(long id, ProductUpdateRequest request);
    void deleteProduct(long id);
    Page<ProductListResponse> searchByKeyword(String keyword, int page);
}
