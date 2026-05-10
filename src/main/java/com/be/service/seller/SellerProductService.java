package com.be.service.seller;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SellerProductService {
    Page<Product> getListByPage(long lastId, int page);
    Product getDetails(long id);
    Page<Product> getListByStatus(Boolean isActive, long lastId, int page);
    Product createProduct(ProductCreateRequest request);
    Product updateProduct(long id, ProductUpdateRequest request);
    void deleteProduct(long id);
    Page<Product> searchByKeyword(String keyword, int page);
}
