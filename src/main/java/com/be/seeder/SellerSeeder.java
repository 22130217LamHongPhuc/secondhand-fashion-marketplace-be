package com.be.seeder;

import com.be.common.enums.AuthProvider;
import com.be.common.enums.ComplaintSeverity;
import com.be.common.enums.ComplaintStatus;
import com.be.common.enums.ComplaintType;
import com.be.common.enums.OrderStatus;
import com.be.common.enums.PaymentMethod;
import com.be.common.enums.PaymentStatus;
import com.be.common.enums.ProductCondition;
import com.be.common.enums.UserRole;
import com.be.entity.Category;
import com.be.entity.Complaint;
import com.be.entity.Order;
import com.be.entity.OrderItem;
import com.be.entity.OrderStatusLog;
import com.be.entity.Product;
import com.be.entity.ProductAttribute;
import com.be.entity.ProductImage;
import com.be.entity.ProductTag;
import com.be.entity.Role;
import com.be.entity.Shop;
import com.be.entity.User;
import com.be.entity.UserAddress;
import com.be.entity.UserRoleMapping;
import com.be.repository.CategoryRepository;
import com.be.repository.ComplaintRepository;
import com.be.repository.OrderRepository;
import com.be.repository.ProductRepository;
import com.be.repository.RoleRepository;
import com.be.repository.ShopRepository;
import com.be.repository.UserAddressRepository;
import com.be.repository.UserRepository;
import com.be.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class SellerSeeder implements CommandLineRunner {

    private static final String SELLER_EMAIL = "seller.demo@secondhand.local";
    private static final String CUSTOMER_EMAIL = "customer.demo@secondhand.local";
    private static final String SHOP_SLUG = "seller-demo-shop";
    private static final int EXTRA_PRODUCTS_PER_STATUS = 12;
    private static final int ORDERS_PER_STATUS = 12;

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ComplaintRepository complaintRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void run(String... args) {
        User seller = seedSeller();
        User customer = seedCustomer();
        UserAddress customerAddress = seedCustomerAddress(customer);
        Shop shop = seedShop(seller);

        if (productRepository.count() > 0) {
            seedComplaints(customer, shop);
            return;
        }

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

        products.addAll(seedProductBatch(shop, outerwear, denim, accessories, true));
        products.addAll(seedProductBatch(shop, outerwear, denim, accessories, false));

        seedOrdersByStatus(customer, customerAddress, shop, products);

        seedComplaints(customer, shop);
    }

    private void seedComplaints(User customer, Shop shop) {
        if (complaintRepository.count() > 0) {
            return;
        }

        Order orderDone = orderRepository.findByOrderCode("SELLER-DEMO-DONE").orElse(null);
        Order orderShipping = orderRepository.findByOrderCode("SELLER-DEMO-SHIPPING").orElse(null);
        Order orderConfirmed = orderRepository.findByOrderCode("SELLER-DEMO-CONFIRMED").orElse(null);

        List<Complaint> complaints = List.of(
                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .order(orderDone)
                        .type(ComplaintType.SHOP_COMPLAINT)
                        .title("Sản phẩm rách nát, khác với hình ảnh mô tả")
                        .content("Tôi mua áo blazer linen với giá 410.000đ nhưng nhận về áo bị rách tay rất to và bẩn. Shop từ chối giải quyết hoàn trả hàng. Đề nghị ban quản trị can thiệp!")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.HIGH)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .order(orderShipping)
                        .type(ComplaintType.SHOP_COMPLAINT)
                        .title("Shop không chịu gửi hàng dù đơn hàng đã thanh toán")
                        .content("Tôi đã thanh toán qua thẻ ngân hàng từ 3 ngày trước, đơn hàng báo đang giao nhưng tôi liên hệ shop hỏi mã vận đơn thì không trả lời tin nhắn.")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.MEDIUM)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .order(orderConfirmed)
                        .type(ComplaintType.SHOP_COMPLAINT)
                        .title("Shop giao hàng nhái, nghi ngờ hàng giả thương hiệu")
                        .content("Tôi đặt mua sản phẩm được ghi là chính hãng vintage Chanel nhưng khi nhận hàng da rất khét mùi nhựa, logo bị lệch và bong tróc. Shop từ chối hoàn trả tiền.")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.HIGH)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .order(orderDone)
                        .type(ComplaintType.SHOP_COMPLAINT)
                        .title("Yêu cầu hoàn trả hàng do giao chậm trễ")
                        .content("Tôi đặt mua sản phẩm để đi tiệc nhưng shop chuẩn bị hàng quá lâu dẫn đến đơn vị vận chuyển giao trễ 2 ngày. Tôi không còn nhu cầu sử dụng nữa nên muốn hoàn tiền.")
                        .status(ComplaintStatus.REJECTED)
                        .severity(ComplaintSeverity.MEDIUM)
                        .resolution("Từ chối khiếu nại. Qua xác minh hệ thống, đơn hàng vẫn được giao thành công trong vòng 3 ngày làm việc (đúng cam kết thời gian vận chuyển). Việc trễ hẹn cá nhân không thuộc chính sách hoàn trả hàng.")
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .type(ComplaintType.USER_FEEDBACK)
                        .title("Ứng dụng thỉnh thoảng giật lag khi kéo xem danh sách")
                        .content("Tôi dùng điện thoại Android kéo lướt xem đồ secondhand thỉnh thoảng giao diện bị đứng hình khoảng 2 giây rồi mới load tiếp. Hy vọng đội kỹ thuật tối ưu hóa.")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.LOW)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .type(ComplaintType.USER_FEEDBACK)
                        .title("Lỗi không tải được ảnh sản phẩm khi đăng bán đồ thanh lý")
                        .content("Tôi bấm đăng bán quần áo cũ nhưng khi tải ảnh lên toàn bị báo lỗi 'Lỗi upload ảnh (500)'. Tôi đã thử nén ảnh nhỏ lại vẫn không được, làm tôi không thể thanh lý đồ được.")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.MEDIUM)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .type(ComplaintType.USER_FEEDBACK)
                        .title("Kiến nghị thêm bộ lọc theo Độ mới (Condition) sản phẩm")
                        .content("Mua bán đồ cũ thì tình trạng đồ (like new, good, fair) rất quan trọng. Mong nhà phát triển thêm bộ lọc nhanh theo độ mới để dễ lựa chọn đồ phù hợp.")
                        .status(ComplaintStatus.PENDING)
                        .severity(ComplaintSeverity.LOW)
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .type(ComplaintType.USER_FEEDBACK)
                        .title("Báo cáo tài khoản giả mạo lừa đảo chuyển khoản ngoài hệ thống")
                        .content("Tài khoản tên 'ShopGiaSi102' nhắn tin dụ dỗ tôi giao dịch qua Zalo nhận giảm giá 20% rồi quỵt cọc của tôi. Đề nghị admin rà soát và khóa vĩnh viễn shop lừa đảo này.")
                        .status(ComplaintStatus.RESOLVED)
                        .severity(ComplaintSeverity.HIGH)
                        .resolution("Cảm ơn bạn đã báo cáo. Ban quản trị đã rà soát lịch sử tin nhắn của shop này, xác nhận hành vi dụ dỗ giao dịch ngoài luồng và đã thực hiện khóa tài khoản vĩnh viễn cùng địa chỉ IP liên quan.")
                        .build(),

                Complaint.builder()
                        .reporter(customer)
                        .reportedShop(shop)
                        .type(ComplaintType.USER_FEEDBACK)
                        .title("Góp ý bổ sung thêm cổng thanh toán ví điện tử Momo")
                        .content("Hiện tại hệ thống chỉ hỗ trợ thanh toán qua chuyển khoản và ví Tiệm Cũ, tôi mong muốn có thêm Momo để thanh toán tiện lợi hơn.")
                        .status(ComplaintStatus.RESOLVED)
                        .severity(ComplaintSeverity.LOW)
                        .resolution("Chào bạn, chân thành cảm ơn đóng góp rất hữu ích của bạn! Tiệm Cũ đã ghi nhận và đang lên kế hoạch tích hợp cổng Momo trong quý tiếp theo.")
                        .build()
        );

        complaintRepository.saveAll(complaints);
    }

    private User seedSeller() {
        User seller = userRepository.findByEmail(SELLER_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(SELLER_EMAIL)
                        .passwordHash(encoder.encode("{noop}password"))
                        .fullName("Seller Demo")
                        .phone("0900000001")
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build()));

        assignRole(seller, UserRole.SELLER);
        return seller;
    }

    private User seedCustomer() {
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(CUSTOMER_EMAIL)
                        .passwordHash(encoder.encode("{noop}password"))
                        .fullName("Customer Demo")
                        .phone("0900000002")
                        .authProvider(AuthProvider.EMAIL)
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build()));

        assignRole(customer, UserRole.CUSTOMER);
        return customer;
    }

    private Role seedRole(UserRole userRole) {
        return roleRepository.findByName(userRole)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(userRole)
                        .build()));
    }

    private void assignRole(User user, UserRole userRole) {
        Role role = seedRole(userRole);

        if (userRoleMappingRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            return;
        }

        userRoleMappingRepository.save(UserRoleMapping.builder()
                .user(user)
                .role(role)
                .build());
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
                        .imageKey("seeder/" + slug + "-front")
                        .sortOrder(0)
                        .isPrimary(true)
                        .build(),

                ProductImage.builder()
                        .product(product)
                        .url("https://placehold.co/800x1000?text=" + slug + "-detail")
                        .imageKey("seeder/" + slug + "-detail")
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

    private List<Product> seedProductBatch(
            Shop shop,
            Category outerwear,
            Category denim,
            Category accessories,
            boolean isActive
    ) {
        List<Category> categories = List.of(outerwear, denim, accessories);
        List<String> productTypes = List.of("Ao so mi", "Quan tay", "Chan vay", "Ao khoac", "Tui deo cheo", "Khan lua");
        List<String> colors = List.of("Trang", "Den", "Xanh navy", "Xam", "Nau", "Xanh reu");
        List<String> sizes = List.of("S", "M", "L", "XL", "Free size");

        List<ProductCondition> conditions = List.of(
                ProductCondition.NEW,
                ProductCondition.LIKE_NEW,
                ProductCondition.GOOD,
                ProductCondition.FAIR
        );

        List<Product> products = new ArrayList<>();
        String statusText = isActive ? "active" : "inactive";

        for (int i = 1; i <= EXTRA_PRODUCTS_PER_STATUS; i++) {
            String productType = productTypes.get((i - 1) % productTypes.size());
            String color = colors.get((i - 1) % colors.size());
            String size = sizes.get((i - 1) % sizes.size());

            BigDecimal basePrice = BigDecimal.valueOf(240000L + (long) i * 35000L);
            BigDecimal salePrice = basePrice.subtract(BigDecimal.valueOf(25000L));

            products.add(seedProduct(
                    shop,
                    categories.get((i - 1) % categories.size()),
                    String.format("%s secondhand %s %02d", productType, statusText, i),
                    String.format("Du lieu mau %s de kiem thu phan trang danh sach san pham seller.", statusText),
                    i % 2 == 0 ? "Vintage Select" : "Local Brand",
                    i % 3 == 0 ? "Japan" : "Vietnam",
                    conditions.get((i - 1) % conditions.size()),
                    basePrice.toPlainString(),
                    salePrice.toPlainString(),
                    isActive ? 3 + i : 0,
                    isActive,
                    List.of(statusText, "seller-demo", productType.toLowerCase().replace(" ", "-")),
                    List.of(attribute("size", size), attribute("color", color), attribute("batch", statusText))
            ));
        }

        return products;
    }

    private void seedOrdersByStatus(
            User customer,
            UserAddress customerAddress,
            Shop shop,
            List<Product> products
    ) {
        for (OrderStatus status : OrderStatus.values()) {
            for (int i = 1; i <= ORDERS_PER_STATUS; i++) {
                Product product = products.get((i - 1) % products.size());

                seedOrder(
                        String.format("SELLER-%s-%02d", status.name(), i),
                        customer,
                        customerAddress,
                        shop,
                        status,
                        paymentStatusFor(status),
                        paymentMethodFor(i),
                        status == OrderStatus.CANCELLED ? "Khach doi y sau khi dat hang" : null,
                        product,
                        i % 3 + 1
                );
            }
        }
    }

    private PaymentStatus paymentStatusFor(OrderStatus status) {
        return switch (status) {
            case PENDING -> PaymentStatus.UNPAID;
            case CANCELLED -> PaymentStatus.REFUNDED;
            case CONFIRMED, SHIPPING, DONE -> PaymentStatus.PAID;
        };
    }

    private PaymentMethod paymentMethodFor(int index) {
        PaymentMethod[] methods = PaymentMethod.values();
        return methods[(index - 1) % methods.length];
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