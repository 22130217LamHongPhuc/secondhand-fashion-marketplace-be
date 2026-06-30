package com.be.service.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.seller.ProductListResponse;
import com.be.dto.response.seller.ProductDetailResponse;
import com.be.dto.response.seller.ProductMutationResponse;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SellerProductService {
    Page<ProductListResponse> searchProducts(String keyword, Boolean isActive, Boolean isApproved, LocalDateTime fromDate, LocalDateTime toDate, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page);
    ProductDetailResponse getDetails(long id);
    ProductMutationResponse createProduct(ProductCreateRequest request);
    ProductMutationResponse updateProduct(long id, ProductUpdateRequest request);
    void deleteProduct(long id);
}
