package com.be.service;

public interface ProductExportService {
    void exportProductsAsync(Long shopId, String subscriberId);
}
