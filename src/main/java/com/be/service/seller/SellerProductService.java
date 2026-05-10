package com.be.service.seller;

import com.be.entity.Product;

import java.util.List;

public interface SellerProductService {
    List<Product> getListByPage(long lastId, int page, int size);
    Product getDetails(long id);
    List<Product> getListByStatus(Boolean isActive, long lastId, int page, int size);
    Product createProduct();
    Product updateProduct(long id);
    void deleteProduct(long id);
}
