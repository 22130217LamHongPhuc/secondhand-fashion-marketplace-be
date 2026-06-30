package com.be.service.seller.impl;

import com.be.dto.request.seller.ProductCreateRequest;
import com.be.common.enums.ProductCondition;
import com.be.constant.Constant;
import com.be.dto.request.seller.ProductAttributeRequest;
import com.be.dto.request.seller.ProductImageRequest;
import com.be.dto.request.seller.ProductUpdateRequest;
import com.be.dto.response.seller.ProductListResponse;
import com.be.dto.response.seller.ProductDetailResponse;
import com.be.dto.response.seller.ProductMutationResponse;
import com.be.dto.response.seller.mapper.SellerProductMapper;
import com.be.entity.Category;
import com.be.entity.Product;
import com.be.entity.ProductAttribute;
import com.be.entity.ProductImage;
import com.be.entity.ProductTag;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.CategoryRepository;
import com.be.repository.ProductRepository;
import com.be.security.AuthHelper;
import com.be.service.ImageUploadExecutorService;
import com.be.service.seller.SellerProductService;
import com.be.utils.KeyGeneratorUtil;
import com.be.utils.UrlGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.be.service.ImageStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.be.repository.specification.ProductSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProductServiceImpl implements SellerProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuthHelper authHelper;
    private final ImageStoreService imageStoreService;

    @Value("${cloudflare.r2.domain}")
    private String cloudflareDomain;

    @Override
    public Page<ProductListResponse> searchProducts(String keyword, Boolean isActive, Boolean isApproved, LocalDateTime fromDate, LocalDateTime toDate, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page) {
        Shop shop = authHelper.getCurrentSellerShop();
        Specification<Product> spec = ProductSpecification.buildFilter(shop.getId(), keyword, isActive, isApproved, fromDate, toDate, minPrice, maxPrice);
        Sort sort = resolveProductSort(sortBy);
        Page<Product> productPage = productRepository.findAll(spec, PageRequest.of(page, Constant.PRODUCT_SIZE, sort));
        List<Long> ids = productPage.getContent().stream().map(Product::getId).toList();
        List<Product> productsWithImages = productRepository.findAllWithImagesByIds(ids);
        java.util.Map<Long, Product> productMap = productsWithImages.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        return productPage.map(p -> SellerProductMapper.toListResponse(productMap.getOrDefault(p.getId(), p)));
    }

    private Sort resolveProductSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by("createdAt").descending();
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by("salePrice").ascending();
            case "price_desc" -> Sort.by("salePrice").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }


    @Override
    public ProductDetailResponse getDetails(long id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + id));
        return SellerProductMapper.toDetailResponse(product);
    }


    @Override
    @Transactional(rollbackFor = {IOException.class, IllegalArgumentException.class})
    public ProductMutationResponse createProduct(ProductCreateRequest request) {
        Shop shop = authHelper.getCurrentSellerShop();
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
                .isActive(false) // Admin approval required
                .isApproved(false)
                .build();

        product.setImages(uploadAndBuildImages(product, request.images()));
        product.setAttributes(buildAttributes(product, request.attributes()));
        product.setTags(buildTags(product, request.tags()));

        Product savedProduct = productRepository.save(product);
        return SellerProductMapper.toMutationResponse(savedProduct);
    }

    private List<ProductImage> uploadAndBuildImages(Product product, List<ProductImageRequest> images) {
        return images.stream().map(image -> {
            String originalUrl = image.imageUrl();
            String targetUrl = originalUrl;
            
            try {
                if (originalUrl != null && !originalUrl.isBlank()) {
                    String key = KeyGeneratorUtil.extractKey(originalUrl);
                    if (key.startsWith(KeyGeneratorUtil.FOLDER_TEMP)) {
                        targetUrl = UrlGenerator.convertTempUrlToProductUrl(originalUrl);
                        String targetKey = KeyGeneratorUtil.extractKey(targetUrl);
                        imageStoreService.copyImage(key, targetKey);
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi khi copy ảnh sản phẩm từ thư mục temp: {}", e.getMessage(), e);
            }
            
            return ProductImage.builder()
                .url(targetUrl)
                .product(product)
                .imageKey(KeyGeneratorUtil.extractKey(targetUrl))
                .isPrimary(image.isPrimary())
                .sortOrder(image.sortOrder())
                .build();
        }).toList();
    }

    @Override
    @Transactional
    public ProductMutationResponse updateProduct(long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + id));

        Shop currentShop = authHelper.getCurrentSellerShop();
        if (!Objects.equals(product.getShop().getId(), currentShop.getId())) {
            throw new IllegalStateException("Bạn không có quyền cập nhật sản phẩm này");
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
            if (!request.isActive()) {
                product.setIsActive(false);
            } else if (!product.getIsActive()) {
                throw new IllegalStateException("Không thể tự kích hoạt sản phẩm. Vui lòng chờ Admin phê duyệt.");
            }
        }

        validatePrice(product.getBasePrice(), product.getSalePrice());

        if (request.images() != null) {
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }
            
            List<String> oldUrls = product.getImages().stream()
                    .map(ProductImage::getUrl)
                    .filter(Objects::nonNull)
                    .toList();
                    
            List<String> newUrls = request.images().stream()
                    .map(ProductImageRequest::imageUrl)
                    .filter(Objects::nonNull)
                    .toList();
                    
            List<String> deletedUrls = oldUrls.stream()
                    .filter(u -> !newUrls.contains(u))
                    .toList();
                    
            for (String deletedUrl : deletedUrls) {
                try {
                    imageStoreService.moveImageToTemp(deletedUrl);
                } catch (Exception e) {
                    log.error("Không thể đưa ảnh bị xóa về temp: {}", deletedUrl, e);
                }
            }
            
            product.getImages().clear();
            product.getImages().addAll(uploadAndBuildImages(product, request.images()));
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

        Product savedProduct = productRepository.save(product);
        return SellerProductMapper.toMutationResponse(savedProduct);
    }

    @Override
    public void deleteProduct(long id) {
        throw new UnsupportedOperationException("Chưa được triển khai");
    }


    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với id: " + categoryId));
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
            throw new IllegalArgumentException("Giá bán phải nhỏ hơn hoặc bằng giá gốc");
        }
    }

}
