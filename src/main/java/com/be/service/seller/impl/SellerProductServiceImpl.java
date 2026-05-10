package com.be.service.seller.impl;

import com.be.entity.Product;
import com.be.service.seller.SellerProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerProductServiceImpl implements SellerProductService {
    @Override
    public List<Product> getListByPage(long lastId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Product getDetails(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Product> getListByStatus(Boolean isActive, long lastId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Product createProduct() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Product updateProduct(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteProduct(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
