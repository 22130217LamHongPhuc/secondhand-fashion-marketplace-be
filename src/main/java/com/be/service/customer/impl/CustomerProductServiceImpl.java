package com.be.service.customer.impl;

import com.be.common.enums.OrderStatus;
import com.be.dto.request.customer.CommentCreateRequest;
import com.be.entity.Comment;
import com.be.common.enums.UserRole;
import com.be.dto.request.customer.ReviewCreateRequest;
import com.be.dto.response.customer.*;
import com.be.entity.Product;
import com.be.entity.ProductImage;
import com.be.entity.ProductTag;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.Review;
import com.be.entity.ReviewImage;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.*;
import jakarta.persistence.EntityNotFoundException;
import com.be.service.ImageUploadExecutorService;
import com.be.service.customer.CustomerProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerProductServiceImpl implements CustomerProductService {
    private static final int FEATURED_SHOP_LIMIT = 10;

    @Value("${cloudflare.r2.domain}")
    private String cloudflareDomain;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ImageUploadExecutorService imageUploadExecutorService;

    @Override
    public List<CategoryItemResponse> getCurrentCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(category -> new CategoryItemResponse(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getIconUrl(),
                        category.getSortOrder()))
                .toList();
    }

    @Override
    public List<ProductCardResponse> getHotDeals(int limit) {
        return productRepository.findHotDeals(PageRequest.of(0, limit)).stream()
                .map(this::toProductCard)
                .toList();
    }

    @Override
    public List<ProductCardResponse> getNewArrivals(int limit) {
        return productRepository.findNewArrivals(PageRequest.of(0, limit)).stream()
                .map(this::toProductCard)
                .toList();
    }

    @Override
    public List<ProductCardResponse> getFeaturedShopsProducts(int limit) {
        LocalDateTime startAt = LocalDateTime.now().minusDays(7);
        List<Long> featuredShopIds = orderRepository.findTopShopIdsByOrderCountSince(
                startAt,
                OrderStatus.CANCELLED,
                PageRequest.of(0, FEATURED_SHOP_LIMIT));

        if (featuredShopIds.isEmpty()) {
            return List.of();
        }

        return productRepository.findByFeaturedShopIds(featuredShopIds, PageRequest.of(0, limit)).stream()
                .map(this::toProductCard)
                .toList();
    }

    @Override
    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        return toProductDetail(product);
    }

    @Override
    public ShopDetailWithProductsResponse getShopDetailWithProducts(Long shopId, int page, int size) {
        Shop shop = shopRepository.findByIdAndIsActiveTrue(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found with id: " + shopId));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var productPage = productRepository.findByShopIdAndIsActiveTrue(shopId, pageable);
        var items = productPage.getContent().stream()
                .map(this::toProductCard)
                .toList();

        return new ShopDetailWithProductsResponse(
                new ShopInfoResponse(
                        shop.getId(),
                        shop.getName(),
                        shop.getSlug(),
                        shop.getDescription(),
                        shop.getAvatarUrl(),
                        shop.getBannerUrl(),
                        shop.getRatingAvg(),
                        shop.getTotalReviews(),
                        shop.getIsActive(),
                        shop.getIsVerified(),
                        shop.getCreatedAt(),
                        shop.getProvinceId(),
                        shop.getProvinceName(),
                        shop.getDistrictId(),
                        shop.getDistrictName(),
                        shop.getWardCode(),
                        shop.getWardName(),
                        shop.getAddressDetail()),
                new ShopProductPageResponse(
                        items,
                        productPage.getNumber(),
                        productPage.getSize(),
                        productPage.getTotalElements(),
                        productPage.getTotalPages(),
                        productPage.hasNext(),
                        productPage.hasPrevious()));
    }

    @Override
    public ShopProductPageResponse getProductsByCategory(Long categoryId, int page, int size) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId is required");
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var productPage = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);

        var items = productPage.getContent().stream()
                .map(this::toProductCard)
                .toList();

        return new ShopProductPageResponse(
                items,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious());
    }

    @Override
    public ShopPageResponse listShops(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var shopPage = shopRepository.findByIsActiveTrue(pageable);

        var items = shopPage.getContent().stream()
                .map(shop -> new ShopInfoResponse(
                        shop.getId(),
                        shop.getName(),
                        shop.getSlug(),
                        shop.getDescription(),
                        shop.getAvatarUrl(),
                        shop.getBannerUrl(),
                        shop.getRatingAvg(),
                        shop.getTotalReviews(),
                        shop.getIsActive(),
                        shop.getIsVerified(),
                        shop.getCreatedAt(),
                        shop.getProvinceId(),
                        shop.getProvinceName(),
                        shop.getDistrictId(),
                        shop.getDistrictName(),
                        shop.getWardCode(),
                        shop.getWardName(),
                        shop.getAddressDetail()))
                .toList();

        return new ShopPageResponse(
                items,
                shopPage.getNumber(),
                shopPage.getSize(),
                shopPage.getTotalElements(),
                shopPage.getTotalPages(),
                shopPage.hasNext(),
                shopPage.hasPrevious());
    }

    @Override
    public ShopPageResponse searchShopsByName(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return listShops(page, size);
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var shopPage = shopRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword.trim(), pageable);

        var items = shopPage.getContent().stream()
                .map(shop -> new ShopInfoResponse(
                        shop.getId(),
                        shop.getName(),
                        shop.getSlug(),
                        shop.getDescription(),
                        shop.getAvatarUrl(),
                        shop.getBannerUrl(),
                        shop.getRatingAvg(),
                        shop.getTotalReviews(),
                        shop.getIsActive(),
                        shop.getIsVerified(),
                        shop.getCreatedAt(),
                        shop.getProvinceId(),
                        shop.getProvinceName(),
                        shop.getDistrictId(),
                        shop.getDistrictName(),
                        shop.getWardCode(),
                        shop.getWardName(),
                        shop.getAddressDetail()))
                .toList();

        return new ShopPageResponse(
                items,
                shopPage.getNumber(),
                shopPage.getSize(),
                shopPage.getTotalElements(),
                shopPage.getTotalPages(),
                shopPage.hasNext(),
                shopPage.hasPrevious());
    }

    @Override
    public ShopProductPageResponse filterAndSortProducts(
            String keyword,
            List<Long> categoryIds,
            String condition,
            List<String> brands,
            List<String> origins,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size) {
        Sort sortObj = mapSortParameter(sort);
        var pageable = PageRequest.of(page, size, sortObj);

        Specification<Product> spec = isActiveTrue();
        spec = andIfPresent(spec, stockGreaterThanZero());
        spec = andIfPresent(spec, keywordContains(keyword));
        spec = andIfPresent(spec, categoriesIn(categoryIds));
        spec = andIfPresent(spec, conditionEquals(condition));
        spec = andIfPresent(spec, brandsIn(brands));
        spec = andIfPresent(spec, originsIn(origins));
        spec = andIfPresent(spec, priceBetween(minPrice, maxPrice));

        var productPage = productRepository.findAll(spec, pageable);

        var items = productPage.getContent().stream()
                .map(this::toProductCard)
                .toList();

        return new ShopProductPageResponse(
                items,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious());
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        User currentUser = getAuthenticatedUser();
        Product product = productRepository.findByIdAndIsActiveTrue(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + request.productId()));

        Comment parentComment = null;
        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent comment not found with id: " + request.parentId()));
        }

        Comment comment = Comment.builder()
                .product(product)
                .user(currentUser)
                .parent(parentComment)
                .content(request.content().trim())
                .isVisible(true)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return toCommentResponse(savedComment);
    }

    @Override
    public CommentPageResponse getProductComments(Long productId, int page, int size) {
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var commentPage = commentRepository.findByProductIdAndIsVisibleTrue(productId, pageable);

        var items = commentPage.getContent().stream()
                .map(this::toCommentResponse)
                .toList();

        return new CommentPageResponse(
                items,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.hasNext(),
                commentPage.hasPrevious());
    }

    @Override
    public ReviewPageResponse getProductReviews(Long productId, int page, int size) {
        productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var reviewPage = reviewRepository.findByProductId(productId, pageable);

        var items = reviewPage.getContent().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVisible()))
                .map(this::toProductDetailReview)
                .toList();

        return new ReviewPageResponse(
                items,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.hasNext(),
                reviewPage.hasPrevious());
    }

    private Sort mapSortParameter(String sort) {
        if (sort == null)
            return Sort.by(Sort.Direction.DESC, "createdAt", "id");
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "salePrice", "basePrice", "id");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "salePrice", "basePrice", "id");
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAvg", "totalReviews", "id");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt", "id");
            default -> Sort.by(Sort.Direction.DESC, "createdAt", "id");
        };
    }

    private Specification<Product> isActiveTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    private Specification<Product> stockGreaterThanZero() {
        return (root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0);
    }

    private Specification<Product> andIfPresent(Specification<Product> base, Specification<Product> next) {
        return next == null ? base : base.and(next);
    }

    private Specification<Product> keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank())
            return null;

        String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), likeKeyword),
                cb.like(cb.lower(root.get("brand")), likeKeyword));
    }

    private Specification<Product> categoriesIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty())
            return null;
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    private Specification<Product> conditionEquals(String condition) {
        if (condition == null || condition.isBlank())
            return null;
        return (root, query, cb) -> cb.equal(root.get("condition"),
                com.be.common.enums.ProductCondition.valueOf(condition));
    }

    private Specification<Product> brandsIn(List<String> brands) {
        if (brands == null || brands.isEmpty())
            return null;
        return (root, query, cb) -> root.get("brand").in(brands);
    }

    private Specification<Product> originsIn(List<String> origins) {
        if (origins == null || origins.isEmpty())
            return null;
        return (root, query, cb) -> root.get("originCountry").in(origins);
    }

    private Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null)
            return null;
        return (root, query, cb) -> {
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("salePrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("salePrice"), minPrice);
            } else {
                return cb.lessThanOrEqualTo(root.get("salePrice"), maxPrice);
            }
        };
    }

    @Override
    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        User currentUser = getAuthenticatedUser();
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + request.orderId()));

        validateReviewOwnership(currentUser, order, request.productId());

        if (reviewRepository.findByOrderIdAndProductId(request.orderId(), request.productId()).isPresent()) {
            throw new IllegalStateException("Review already exists for this order and product");
        }

        OrderItem orderItem = findOrderItem(order, request.productId());
        Product product = orderItem.getProduct();

        Review review = Review.builder()
                .product(product)
                .user(currentUser)
                .order(order)
                .rating(request.rating().byteValue())
                .comment(request.comment())
                .isVisible(true)
                .build();

        List<ReviewImage> images = uploadReviewImages(review, request.images());
        review.setImages(images);

        Review savedReview = reviewRepository.save(review);
        return toReviewCreateResponse(savedReview);
    }

    private ProductCardResponse toProductCard(Product product) {
        BigDecimal discountAmount = getDiscountAmount(product.getBasePrice(), product.getSalePrice());
        return new ProductCardResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getSalePrice(),
                discountAmount,
                getThumbnailUrl(product),
                product.getShop() == null ? null : product.getShop().getId(),
                product.getShop() == null ? null : product.getShop().getName(),
                product.getCreatedAt());
    }

    private ProductDetailResponse toProductDetail(Product product) {
        BigDecimal discountAmount = getDiscountAmount(product.getBasePrice(), product.getSalePrice());

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getOriginCountry(),
                product.getCondition() == null ? null : product.getCondition().name(),
                product.getBasePrice(),
                product.getSalePrice(),
                discountAmount,
                product.getStockQuantity(),
                product.getRatingAvg(),
                product.getTotalReviews(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                getThumbnailUrl(product),
                mapImages(product),
                mapAttributes(product),
                mapTags(product),
                product.getCategory() == null ? null
                        : new ProductDetailCategoryResponse(
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getCategory().getSlug()),
                product.getShop() == null ? null
                        : new ProductDetailShopResponse(
                                product.getShop().getId(),
                                product.getShop().getName(),
                                product.getShop().getSlug(),
                                product.getShop().getAvatarUrl(),
                                product.getShop().getIsVerified(),
                                product.getShop().getRatingAvg(),
                                product.getShop().getTotalReviews(),
                                product.getShop().getSeller() == null ? null : product.getShop().getSeller().getId(),
                                product.getShop().getProvinceId(),
                                product.getShop().getProvinceName(),
                                product.getShop().getDistrictId(),
                                product.getShop().getDistrictName(),
                                product.getShop().getWardCode(),
                                product.getShop().getWardName(),
                                product.getShop().getAddressDetail()),
                mapLatestComments(product.getId()),
                mapLatestReviews(product.getId()),
                mapRelatedProducts(product));
    }

    private List<ProductDetailCommentResponse> mapLatestComments(Long productId) {
        if (productId == null) {
            return List.of();
        }

        return commentRepository.findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(productId).stream()
                .map(this::toProductDetailComment)
                .toList();
    }

    private ProductDetailCommentResponse toProductDetailComment(com.be.entity.Comment comment) {
        return new ProductDetailCommentResponse(
                comment.getId(),
                comment.getUser() == null ? null : comment.getUser().getId(),
                comment.getUser() == null ? null : comment.getUser().getFullName(),
                comment.getUser() == null ? null : comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt());
    }

    private BigDecimal getDiscountAmount(BigDecimal basePrice, BigDecimal salePrice) {
        if (basePrice == null || salePrice == null || basePrice.compareTo(salePrice) <= 0) {
            return BigDecimal.ZERO;
        }
        return basePrice.subtract(salePrice);
    }

    private String getThumbnailUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .findFirst()
                .or(() -> product.getImages().stream()
                        .filter(image -> image.getUrl() != null)
                        .min(Comparator.comparing(
                                ProductImage::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo))))
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    private List<ProductDetailImageResponse> mapImages(Product product) {
        if (product.getImages() == null) {
            return List.of();
        }

        return product.getImages().stream()
                .sorted(Comparator
                        .comparing(ProductImage::getIsPrimary, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProductImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ProductImage::getId, Comparator.nullsLast(Long::compareTo)))
                .map(image -> new ProductDetailImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.getSortOrder(),
                        image.getIsPrimary()))
                .toList();
    }

    private List<ProductDetailAttributeResponse> mapAttributes(Product product) {
        if (product.getAttributes() == null) {
            return List.of();
        }

        return product.getAttributes().stream()
                .map(attribute -> new ProductDetailAttributeResponse(
                        attribute.getId(),
                        attribute.getAttrKey(),
                        attribute.getAttrValue()))
                .toList();
    }

    private List<String> mapTags(Product product) {
        if (product.getTags() == null) {
            return List.of();
        }

        return product.getTags().stream()
                .map(ProductTag::getTag)
                .collect(Collectors.toList());
    }

    private List<ProductCardResponse> mapRelatedProducts(Product product) {
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            return List.of();
        }

        return productRepository.findTop8ByCategoryIdAndIsActiveTrueAndIdNotOrderByCreatedAtDesc(
                product.getCategory().getId(),
                product.getId()).stream()
                .map(this::toProductCard)
                .toList();
    }

    private List<ProductDetailReviewResponse> mapLatestReviews(Long productId) {
        if (productId == null) {
            return List.of();
        }

        return reviewRepository.findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(productId).stream()
                .map(this::toProductDetailReview)
                .toList();
    }

    private ProductDetailReviewResponse toProductDetailReview(Review review) {
        List<String> imageUrls = review.getImages() == null ? List.of()
                : review.getImages().stream()
                        .map(img -> img.getUrl())
                        .toList();

        return new ProductDetailReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser() == null ? null : review.getUser().getId(),
                review.getUser() == null ? null : review.getUser().getFullName(),
                review.getUser() == null ? null : review.getUser().getAvatarUrl(),
                imageUrls,
                review.getCreatedAt());
    }

    private ReviewCreateResponse toReviewCreateResponse(Review review) {
        List<String> imageUrls = review.getImages() == null ? List.of()
                : review.getImages().stream()
                        .map(ReviewImage::getUrl)
                        .toList();

        return new ReviewCreateResponse(
                review.getId(),
                review.getOrder() == null ? null : review.getOrder().getId(),
                review.getProduct() == null ? null : review.getProduct().getId(),
                review.getRating(),
                review.getComment(),
                review.getUser() == null ? null : review.getUser().getId(),
                review.getUser() == null ? null : review.getUser().getFullName(),
                review.getUser() == null ? null : review.getUser().getAvatarUrl(),
                imageUrls,
                review.getCreatedAt());
    }

    private User getAuthenticatedUser() {

        return userRepository.findById(501L)
                .orElseThrow(() -> new AccessDeniedException("Authenticated customer is required"));
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // if (authentication == null || !(authentication.getPrincipal() instanceof User
        // user)) {
        // throw new AccessDeniedException("Authenticated customer is required");
        // }
        //
        // if (user.getId() == null) {
        // throw new AccessDeniedException("Authenticated customer is required");
        // }
        //
        // boolean isCustomer = user.getUserRoles().stream()
        // .anyMatch(mapping -> mapping.getRole() != null && mapping.getRole().getName()
        // == UserRole.CUSTOMER);
        // if (!isCustomer) {
        // throw new AccessDeniedException("Only customer can review products");
        // }
        //
        // return user;

    }

    private void validateReviewOwnership(User currentUser, Order order, Long productId) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null
                || !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only review products from your own order");
        }

        if (order.getStatus() != OrderStatus.DONE) {
            throw new IllegalStateException("Only completed orders can be reviewed");
        }

        findOrderItem(order, productId);
    }

    private OrderItem findOrderItem(Order order, Long productId) {
        if (order.getItems() == null) {
            throw new IllegalArgumentException("Product is not in this order");
        }

        return order.getItems().stream()
                .filter(item -> item.getProduct() != null
                        && item.getProduct().getId() != null
                        && item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product is not in this order"));
    }

    private List<ReviewImage> uploadReviewImages(Review review, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<ReviewImage> images = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Review image file is required");
            }

            byte[] data = toByteArray(file);
            String key = joinImageUpload(imageUploadExecutorService.uploadImage(data, file.getOriginalFilename()));
            images.add(ReviewImage.builder()
                    .review(review)
                    .url(buildCloudflareImageUrl(key))
                    .build());
        }

        return images;
    }

    private String joinImageUpload(java.util.concurrent.CompletableFuture<String> uploadTask) {
        try {
            return uploadTask.join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            throw new IllegalStateException("Could not upload review image", cause);
        }
    }

    private String buildCloudflareImageUrl(String key) {
        String normalizedDomain = cloudflareDomain.endsWith("/")
                ? cloudflareDomain.substring(0, cloudflareDomain.length() - 1)
                : cloudflareDomain;
        String normalizedKey = key.startsWith("/") ? key.substring(1) : key;
        return normalizedDomain + "/" + normalizedKey;
    }

    private byte[] toByteArray(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read review image file", exception);
        }
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getProduct() == null ? null : comment.getProduct().getId(),
                comment.getUser() == null ? null : comment.getUser().getId(),
                comment.getUser() == null ? null : comment.getUser().getFullName(),
                comment.getUser() == null ? null : comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}
