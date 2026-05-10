package com.be.service.seller.impl;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.common.enums.ProductCondition;
import com.be.constant.Constant;
import com.be.dto.request.seller.ProductAttributeRequest;
import com.be.dto.request.seller.ProductImageRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.entity.Category;
import com.be.entity.Product;
import com.be.entity.ProductAttribute;
import com.be.entity.ProductImage;
import com.be.entity.ProductTag;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.CategoryRepository;
import com.be.repository.ProductRepository;
import com.be.repository.ShopRepository;
import com.be.service.seller.SellerProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SellerProductServiceImpl implements SellerProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    @Override
    public Page<Product> getListByPage(long lastId, int page) {
        return productRepository.getListByPage(lastId, PageRequest.of(page, Constant.PRODUCT_SIZE));
    }

    @Override
    public Product getDetails(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Page<Product> getListByStatus(Boolean isActive, long lastId, int page) {
        return productRepository.getListByStatus(isActive, lastId, PageRequest.of(page, Constant.PRODUCT_SIZE));
    }

    @Override
    @Transactional
    public Product createProduct(ProductCreateRequest request) {
        Shop shop = getCurrentSellerShop();
        Category category = getCategory(request.categoryId());

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .name(request.name().trim())
                .description(request.description())
                .brand(request.brand())
                .originCountry(request.originCountry())
                .condition(request.condition() == null ? ProductCondition.GOOD : request.condition())
                .basePrice(request.basePrice())
                .salePrice(request.salePrice())
                .stockQuantity(request.stockQuantity())
                .build();

        product.setImages(buildImages(product, request.images()));
        product.setAttributes(buildAttributes(product, request.attributes()));
        product.setTags(buildTags(product, request.tags()));

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        Shop currentShop = getCurrentSellerShop();
        if (!Objects.equals(product.getShop().getId(), currentShop.getId())) {
            throw new IllegalStateException("You do not have permission to update this product");
        }

        if (request.categoryId() != null) {
            product.setCategory(getCategory(request.categoryId()));
        }
        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.brand() != null) {
            product.setBrand(request.brand());
        }
        if (request.originCountry() != null) {
            product.setOriginCountry(request.originCountry());
        }
        if (request.condition() != null) {
            product.setCondition(request.condition());
        }
        if (request.basePrice() != null) {
            product.setBasePrice(request.basePrice());
        }
        if (request.salePrice() != null) {
            product.setSalePrice(request.salePrice());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }

        validatePrice(product.getBasePrice(), product.getSalePrice());

        if (request.images() != null) {
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }
            product.getImages().clear();
            product.getImages().addAll(buildImages(product, request.images()));
        }
        if (request.attributes() != null) {
            if (product.getAttributes() == null) {
                product.setAttributes(new ArrayList<>());
            }
            product.getAttributes().clear();
            product.getAttributes().addAll(buildAttributes(product, request.attributes()));
        }
        if (request.tags() != null) {
            if (product.getTags() == null) {
                product.setTags(new ArrayList<>());
            }
            product.getTags().clear();
            product.getTags().addAll(buildTags(product, request.tags()));
        }

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Page<Product> searchByKeyword(String keyword, int page) {
        return productRepository.searchByKeyword(keyword, PageRequest.of(page, Constant.PRODUCT_SIZE));
    }

    private Shop getCurrentSellerShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Authenticated seller is required");
        }

        return shopRepository.findBySellerId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found for current seller"));
    }

    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
    }

    private List<ProductImage> buildImages(Product product, List<ProductImageRequest> images) {
        if (images == null) {
            return List.of();
        }

        return images.stream()
                .map(image -> ProductImage.builder()
                        .product(product)
                        .url(image.url())
                        .sortOrder(image.sortOrder() == null ? 0 : image.sortOrder())
                        .isPrimary(Boolean.TRUE.equals(image.isPrimary()))
                        .build())
                .toList();
    }

    private List<ProductAttribute> buildAttributes(Product product, List<ProductAttributeRequest> attributes) {
        if (attributes == null) {
            return List.of();
        }

        return attributes.stream()
                .map(attribute -> ProductAttribute.builder()
                        .product(product)
                        .attrKey(attribute.attrKey())
                        .attrValue(attribute.attrValue())
                        .build())
                .toList();
    }

    private List<ProductTag> buildTags(Product product, List<String> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .map(tag -> ProductTag.builder()
                        .product(product)
                        .tag(tag.trim())
                        .build())
                .toList();
    }

    private void validatePrice(BigDecimal basePrice, BigDecimal salePrice) {
        if (salePrice != null && basePrice != null && salePrice.compareTo(basePrice) > 0) {
            throw new IllegalArgumentException("Sale price must be less than or equal to base price");
        }
    }
}
