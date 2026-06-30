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
                .isApproved(true)
                .shop(shopRepository.findById(request.getShopId())
                        .orElseThrow(() -> new RuntimeException("Shop not found")))
                .category(request.getCategoryId() != null ? 
                        categoryRepository.findById(request.getCategoryId()).orElse(null) : null)
                .build();

        if (request.getImageUrls() != null) {
            product.setImages(request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .imageKey(extractImageKey(url))
                            .product(product)
                            .build())
                    .collect(Collectors.toList()));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAllWithDetails();
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setOriginCountry(request.getOriginCountry());
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }

        if (request.getImageUrls() != null) {
            product.getImages().clear();
            product.getImages().addAll(request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .imageKey(extractImageKey(url))
                            .product(product)
                            .build())
                    .collect(Collectors.toList()));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product toggleProductActive(Long id, boolean active) {
        Product product = getProductById(id);
        product.setIsActive(active);
        if (active) {
            product.setIsApproved(true);
        }
        return productRepository.save(product);
    }

    private String extractImageKey(String url) {
        if (url == null) return "";
        int pathStart = url.indexOf("/", 8);
        if (pathStart != -1 && pathStart < url.length() - 1) {
            return url.substring(pathStart + 1);
        }
        return url;
    }
}
