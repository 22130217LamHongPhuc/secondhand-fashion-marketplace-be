package com.be.seeder;

import com.be.common.enums.AuthProvider;
import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.common.enums.ProductCondition;
import com.be.common.enums.UserRole;
import com.be.entity.Category;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.OrderStatusLog;
import com.be.entity.Product;
import com.be.entity.ProductAttribute;
import com.be.entity.ProductImage;
import com.be.entity.ProductTag;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.entity.UserAddress;
import com.be.repository.CategoryRepository;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import com.be.repository.ShopRepository;
import com.be.repository.UserAddressRepository;
import com.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Profile("!prod & !test")
@RequiredArgsConstructor
public class SellerSeeder implements CommandLineRunner {
    private static final String SELLER_EMAIL = "seller.demo@secondhand.local";
    private static final String CUSTOMER_EMAIL = "customer.demo@secondhand.local";
    private static final String SHOP_SLUG = "seller-demo-shop";

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        User seller = seedSeller();
        User customer = seedCustomer();
        UserAddress customerAddress = seedCustomerAddress(customer);
        Shop shop = seedShop(seller);

        Category outerwear = seedCategory("Ao khoac", "ao-khoac", 10);
        Category denim = seedCategory("Denim", "denim", 20);
        Category accessories = seedCategory("Phu kien", "phu-kien", 30);

        List<Product> products = new ArrayList<>();
        products.add(seedProduct(
                shop,
                outerwear,
                "Ao blazer linen vintage",
                "Ao blazer linen da qua su dung, form rong, phu hop di lam va di choi.",
                "Vintage Select",
                "Japan",
                ProductCondition.GOOD,
                "520000",
                "410000",
                7,
                true,
                List.of("blazer", "linen", "vintage"),
                List.of(attribute("size", "M"), attribute("color", "Beige"), attribute("material", "Linen blend"))
        ));
        products.add(seedProduct(
                shop,
                denim,
                "Quan jeans ong dung washed",
                "Quan jeans ong dung mau xanh washed, con tot, khong rach.",
                "Levi's",
                "USA",
                ProductCondition.LIKE_NEW,
                "680000",
                "590000",
                5,
                true,
                List.of("jeans", "denim", "washed"),
                List.of(attribute("size", "30"), attribute("color", "Blue"), attribute("fit", "Straight"))
        ));
        products.add(seedProduct(
                shop,
                outerwear,
                "Ao khoac bomber den",
                "Bomber den co tui trong, phan bo tay hoi xuoc nhe.",
                "Uniqlo",
                "Vietnam",
                ProductCondition.FAIR,
                "450000",
                "320000",
                3,
                true,
                List.of("bomber", "streetwear", "black"),
                List.of(attribute("size", "L"), attribute("color", "Black"), attribute("defect", "Xuoc nhe bo tay"))
        ));
        products.add(seedProduct(
                shop,
                accessories,
                "Tui tote canvas kem",
                "Tui tote canvas day dan, phu hop dung hang ngay.",
                "Local Brand",
                "Vietnam",
                ProductCondition.NEW,
                "260000",
                "220000",
                12,
                true,
                List.of("tote", "canvas", "minimal"),
                List.of(attribute("color", "Cream"), attribute("width", "38cm"), attribute("height", "42cm"))
        ));
        products.add(seedProduct(
                shop,
                accessories,
                "Mu bucket caro ngung ban",
                "San pham mau cu dung de kiem thu danh sach san pham inactive.",
                "No Brand",
                "Korea",
                ProductCondition.GOOD,
                "180000",
                "150000",
                0,
                false,
                List.of("bucket", "inactive", "caro"),
                List.of(attribute("size", "Free size"), attribute("color", "Brown check"))
        ));

        seedOrder(
                "SELLER-DEMO-PENDING",
                customer,
                customerAddress,
                shop,
                OrderStatus.PENDING,
                PaymentStatus.UNPAID,
                PaymentMethod.COD,
                null,
                products.get(0),
                1
        );
        seedOrder(
                "SELLER-DEMO-CONFIRMED",
                customer,
                customerAddress,
                shop,
                OrderStatus.CONFIRMED,
                PaymentStatus.PAID,
                PaymentMethod.WALLET,
                null,
                products.get(1),
                1
        );
        seedOrder(
                "SELLER-DEMO-SHIPPING",
                customer,
                customerAddress,
                shop,
                OrderStatus.SHIPPING,
                PaymentStatus.PAID,
                PaymentMethod.BANK_TRANSFER,
                null,
                products.get(2),
                2
        );
        seedOrder(
                "SELLER-DEMO-DONE",
                customer,
                customerAddress,
                shop,
                OrderStatus.DONE,
                PaymentStatus.PAID,
                PaymentMethod.WALLET,
                null,
                products.get(3),
                1
        );
        seedOrder(
                "SELLER-DEMO-CANCELLED",
                customer,
                customerAddress,
                shop,
                OrderStatus.CANCELLED,
                PaymentStatus.REFUNDED,
                PaymentMethod.WALLET,
                "Khach doi y sau khi dat hang",
                products.get(0),
                1
        );
    }

    private User seedSeller() {
        return userRepository.findByEmail(SELLER_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(SELLER_EMAIL)
                        .passwordHash("{noop}password")
                        .fullName("Seller Demo")
                        .phone("0900000001")
                        .role(UserRole.SELLER)
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build()));
    }

    private User seedCustomer() {
        return userRepository.findByEmail(CUSTOMER_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(CUSTOMER_EMAIL)
                        .passwordHash("{noop}password")
                        .fullName("Customer Demo")
                        .phone("0900000002")
                        .role(UserRole.CUSTOMER)
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build()));
    }

    private UserAddress seedCustomerAddress(User customer) {
        return userAddressRepository.findByUserIdAndIsDefaultTrue(customer.getId())
                .orElseGet(() -> userAddressRepository.save(UserAddress.builder()
                        .user(customer)
                        .fullName(customer.getFullName())
                        .phone(customer.getPhone())
                        .province("Ho Chi Minh")
                        .district("Quan 1")
                        .ward("Ben Nghe")
                        .addressDetail("12 Nguyen Hue")
                        .isDefault(true)
                        .build()));
    }

    private Shop seedShop(User seller) {
        return shopRepository.findBySlug(SHOP_SLUG)
                .orElseGet(() -> shopRepository.save(Shop.builder()
                        .seller(seller)
                        .name("Seller Demo Shop")
                        .slug(SHOP_SLUG)
                        .description("Shop du lieu mau cho seller order va seller product API.")
                        .avatarUrl("https://placehold.co/300x300?text=Seller+Demo")
                        .bannerUrl("https://placehold.co/1200x400?text=Secondhand+Fashion")
                        .ratingAvg(new BigDecimal("4.70"))
                        .totalReviews(24)
                        .isActive(true)
                        .isVerified(true)
                        .build()));
    }

    private Category seedCategory(String name, String slug, int sortOrder) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .sortOrder(sortOrder)
                        .isActive(true)
                        .build()));
    }

    private Product seedProduct(
            Shop shop,
            Category category,
            String name,
            String description,
            String brand,
            String originCountry,
            ProductCondition condition,
            String basePrice,
            String salePrice,
            int stockQuantity,
            boolean isActive,
            List<String> tags,
            List<ProductAttribute> attributes
    ) {
        return productRepository.findAll().stream()
                .filter(product -> Objects.equals(product.getShop().getId(), shop.getId()))
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Product product = Product.builder()
                            .shop(shop)
                            .category(category)
                            .name(name)
                            .description(description)
                            .brand(brand)
                            .originCountry(originCountry)
                            .condition(condition)
                            .basePrice(new BigDecimal(basePrice))
                            .salePrice(new BigDecimal(salePrice))
                            .stockQuantity(stockQuantity)
                            .isActive(isActive)
                            .ratingAvg(new BigDecimal("4.60"))
                            .totalReviews(8)
                            .build();

                    product.setImages(buildImages(product, name));
                    product.setAttributes(bindAttributes(product, attributes));
                    product.setTags(bindTags(product, tags));

                    return productRepository.save(product);
                });
    }

    private void seedOrder(
            String orderCode,
            User customer,
            UserAddress customerAddress,
            Shop shop,
            OrderStatus status,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            String cancelReason,
            Product product,
            int quantity
    ) {
        if (orderRepository.findByOrderCode(orderCode).isPresent()) {
            return;
        }

        BigDecimal unitPrice = product.getSalePrice() == null ? product.getBasePrice() : product.getSalePrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal shippingFee = new BigDecimal("30000");

        Order order = Order.builder()
                .customer(customer)
                .shop(shop)
                .orderCode(orderCode)
                .shippingAddress(customerAddress)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .status(status)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .cancelReason(cancelReason)
                .paidAt(paymentStatus == PaymentStatus.PAID || paymentStatus == PaymentStatus.REFUNDED ? LocalDateTime.now() : null)
                .deliveredAt(status == OrderStatus.DONE ? LocalDateTime.now() : null)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .subtotal(subtotal)
                .build();

        OrderStatusLog log = OrderStatusLog.builder()
                .order(order)
                .status(status)
                .note("Seed data for seller API testing")
                .changedBy(customer)
                .build();

        order.setItems(List.of(item));
        order.setStatusLogs(List.of(log));
        orderRepository.save(order);
    }

    private List<ProductImage> buildImages(Product product, String productName) {
        String slug = productName.toLowerCase()
                .replace(" ", "-")
                .replace("'", "");

        return List.of(
                ProductImage.builder()
                        .product(product)
                        .url("https://placehold.co/800x1000?text=" + slug + "-front")
                        .sortOrder(0)
                        .isPrimary(true)
                        .build(),
                ProductImage.builder()
                        .product(product)
                        .url("https://placehold.co/800x1000?text=" + slug + "-detail")
                        .sortOrder(1)
                        .isPrimary(false)
                        .build()
        );
    }

    private List<ProductAttribute> bindAttributes(Product product, List<ProductAttribute> attributes) {
        attributes.forEach(attribute -> attribute.setProduct(product));
        return attributes;
    }

    private ProductAttribute attribute(String key, String value) {
        return ProductAttribute.builder()
                .attrKey(key)
                .attrValue(value)
                .build();
    }

    private List<ProductTag> bindTags(Product product, List<String> tags) {
        return tags.stream()
                .map(tag -> ProductTag.builder()
                        .product(product)
                        .tag(tag)
                        .build())
                .toList();
    }
}
