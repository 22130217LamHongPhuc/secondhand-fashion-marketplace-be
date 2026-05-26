package com.be.service;

import com.be.dto.request.ProductRequest;
import com.be.entity.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductRequest request);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    Product updateProduct(Long id, ProductRequest request);
}
