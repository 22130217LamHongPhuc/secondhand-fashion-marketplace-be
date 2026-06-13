package com.be.service.customer.impl;

import com.be.common.enums.OrderStatus;
import com.be.common.enums.UserRole;
import com.be.dto.request.customer.ReviewCreateRequest;
import com.be.dto.response.customer.ProductDetailResponse;
import com.be.dto.response.customer.ReviewCreateResponse;
import com.be.dto.response.customer.ShopDetailWithProductsResponse;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.Product;
import com.be.entity.Review;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.repository.CategoryRepository;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import com.be.repository.ReviewRepository;
import com.be.repository.ShopRepository;
import com.be.repository.CommentRepository;
import com.be.repository.UserRepository;
import com.be.service.ImageUploadExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProductServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageUploadExecutorService imageUploadExecutorService;

    @InjectMocks
    private CustomerProductServiceImpl customerProductService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customerProductService, "cloudflareDomain", "https://cdn.example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProductDetail_shouldIncludeLatestVisibleReviews() {
        Product product = Product.builder()
                .id(10L)
                .name("Ao khoac vintage")
                .isActive(true)
                .build();

        User reviewerA = User.builder()
                .id(100L)
                .fullName("Nguyen Van A")
                .avatarUrl("https://cdn.example.com/a.jpg")
                .build();

        User reviewerB = User.builder()
                .id(101L)
                .fullName("Tran Thi B")
                .build();

        Review reviewA = Review.builder()
                .id(1L)
                .product(product)
                .user(reviewerA)
                .rating((byte) 5)
                .comment("San pham dep, dung mo ta")
                .isVisible(true)
                .build();
        reviewA.setCreatedAt(LocalDateTime.of(2026, 5, 18, 12, 0));

        Review reviewB = Review.builder()
                .id(2L)
                .product(product)
                .user(reviewerB)
                .rating((byte) 4)
                .comment("Giao hang nhanh")
                .isVisible(true)
                .build();
        reviewB.setCreatedAt(LocalDateTime.of(2026, 5, 17, 10, 0));

        when(productRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(List.of(reviewA, reviewB));
        when(commentRepository.findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(List.of());

        ProductDetailResponse response = customerProductService.getProductDetail(10L);

        assertThat(response.latestReviews()).hasSize(2);
        assertThat(response.latestReviews().get(0).id()).isEqualTo(1L);
        assertThat(response.latestReviews().get(0).reviewerName()).isEqualTo("Nguyen Van A");
        assertThat(response.latestReviews().get(1).id()).isEqualTo(2L);
        verify(reviewRepository).findTop3ByProductIdAndIsVisibleTrueOrderByCreatedAtDescIdDesc(10L);
    }

    @Test
    void getShopDetailWithProducts_shouldReturnShopAndPaginatedProducts() {
        User seller = User.builder()
                .id(500L)
                .fullName("Shop Owner")
                .email("owner@example.com")
                .build();

        Shop shop = Shop.builder()
                .id(20L)
                .seller(seller)
                .name("Vintage Corner")
                .slug("vintage-corner")
                .ratingAvg(new BigDecimal("4.70"))
                .totalReviews(120)
                .isActive(true)
                .isVerified(true)
                .build();
        shop.setCreatedAt(LocalDateTime.of(2026, 5, 10, 9, 0));

        Product product = Product.builder()
                .id(301L)
                .shop(shop)
                .name("Tui canvas")
                .basePrice(new BigDecimal("350000"))
                .salePrice(new BigDecimal("290000"))
                .isActive(true)
                .build();

        when(shopRepository.findByIdAndIsActiveTrue(20L)).thenReturn(Optional.of(shop));
        when(productRepository.findByShopIdAndIsActiveTrue(any(Long.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));

        ShopDetailWithProductsResponse response = customerProductService.getShopDetailWithProducts(20L, 0, 10);

        assertThat(response.shop().id()).isEqualTo(20L);
        assertThat(response.products().items()).hasSize(1);
        assertThat(response.products().items().get(0).id()).isEqualTo(301L);
    }

    @Test
    void createReview_shouldAllowBuyerWhenOrderDone() {
        User customer = User.builder()
                .id(1L)
                .fullName("Customer Demo")
                .email("customer@example.com")
                .build();

        Product product = Product.builder()
                .id(88L)
                .name("Ao khoac vintage")
                .build();

        Order order = Order.builder()
                .id(77L)
                .customer(customer)
                .status(OrderStatus.DONE)
                .items(new ArrayList<>())
                .build();

        order.getItems().add(OrderItem.builder()
                .order(order)
                .product(product)
                .productName("Ao khoac vintage")
                .unitPrice(new BigDecimal("300000"))
                .quantity(1)
                .subtotal(new BigDecimal("300000"))
                .build());

        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities())
        ));

        when(orderRepository.findById(77L)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderIdAndProductId(77L, 88L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageUploadExecutorService.uploadImage(any(), any()))
                .thenReturn(CompletableFuture.completedFuture("review-images/review-1.jpg"));

        ReviewCreateResponse response = customerProductService.createReview(new ReviewCreateRequest(
                77L,
                88L,
                5,
                "San pham tot",
                List.of(new MockMultipartFile("images", "review.jpg", "image/jpeg", new byte[]{1, 2, 3}))
        ));

        assertThat(response.orderId()).isEqualTo(77L);
        assertThat(response.productId()).isEqualTo(88L);
        assertThat(response.rating()).isEqualTo((byte) 5);
        assertThat(response.imageUrls()).containsExactly("https://cdn.example.com/review-images/review-1.jpg");
    }

    @Test
    void createReview_shouldRejectWhenOrderDoesNotBelongToCurrentUser() {
        User currentUser = User.builder()
                .id(1L)
                .fullName("Customer Demo")
                .email("customer@example.com")
                .build();

        User otherCustomer = User.builder()
                .id(2L)
                .fullName("Other Customer")
                .email("other@example.com")
                .build();

        Product product = Product.builder()
                .id(88L)
                .name("Ao khoac vintage")
                .build();

        Order order = Order.builder()
                .id(77L)
                .customer(otherCustomer)
                .status(OrderStatus.DONE)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .productName("Ao khoac vintage")
                .unitPrice(new BigDecimal("300000"))
                .quantity(1)
                .subtotal(new BigDecimal("300000"))
                .build();
        order.setItems(List.of(orderItem));

        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())
        ));

        when(orderRepository.findById(77L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> customerProductService.createReview(new ReviewCreateRequest(
                77L,
                88L,
                4,
                "Good",
                List.of()
        ))).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void createReview_shouldRejectWhenOrderIsNotDone() {
        User customer = User.builder()
                .id(1L)
                .fullName("Customer Demo")
                .email("customer@example.com")
                .build();

        Product product = Product.builder()
                .id(88L)
                .name("Ao khoac vintage")
                .build();

        Order order = Order.builder()
                .id(77L)
                .customer(customer)
                .status(OrderStatus.SHIPPING)
                .items(new ArrayList<>())
                .build();
        order.getItems().add(OrderItem.builder()
                .order(order)
                .product(product)
                .productName("Ao khoac vintage")
                .unitPrice(new BigDecimal("300000"))
                .quantity(1)
                .subtotal(new BigDecimal("300000"))
                .build());

        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities())
        ));

        when(orderRepository.findById(77L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> customerProductService.createReview(new ReviewCreateRequest(
                77L,
                88L,
                4,
                "Good",
                List.of()
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only completed orders can be reviewed");
    }
}

