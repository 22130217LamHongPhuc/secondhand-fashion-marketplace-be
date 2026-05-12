package com.be.service.impl;

import com.be.dto.request.ProductRequest;
import com.be.entity.Product;
import com.be.entity.ProductImage;
import com.be.repository.CategoryRepository;
import com.be.repository.ProductRepository;
import com.be.repository.ShopRepository;
import com.be.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .originCountry(request.getOriginCountry())
                .condition(request.getCondition())
                .basePrice(request.getBasePrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .isActive(true)
                .shop(shopRepository.findById(request.getShopId())
                        .orElseThrow(() -> new RuntimeException("Shop not found")))
                .category(request.getCategoryId() != null ? 
                        categoryRepository.findById(request.getCategoryId()).orElse(null) : null)
                .build();

        if (request.getImageUrls() != null) {
            product.setImages(request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .product(product)
                            .build())
                    .collect(Collectors.toList()));
        }

        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
