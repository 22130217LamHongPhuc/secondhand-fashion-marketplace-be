# Entity & Repository Mapping - Secondhand Fashion Marketplace

## Tổng quan
Dự án đã hoàn toàn ánh xạ database schema MySQL vào các entity JPA cùng repositories Spring Data.

---

## Danh sách Entity (15 entities)

### 1. **User** (`users` table)
- Đại diện cho người dùng (khách hàng, người bán, admin)
- Enum: `UserRole` (CUSTOMER, SELLER, ADMIN), `AuthProvider` (EMAIL, GOOGLE, FACEBOOK)
- Relationships:
  - 1:1 với `Wallet`
  - 1:N với `UserAddress`
  - 1:1 với `Shop` (nếu là seller)
  - 1:N với `Order` (nếu là customer)
  - 1:N với `Review`
  - 1:N với `Comment`

### 2. **Wallet** (`wallets` table)
- Ví điện tử cho mỗi user
- Lưu số dư tài khoản
- Relationships:
  - 1:1 với `User`
  - 1:N với `WalletTransaction`

### 3. **WalletTransaction** (`wallet_transactions` table)
- Ghi lại mỗi giao dịch ví (nạp tiền, thanh toán, hoàn tiền, rút tiền)
- Enum: `TransactionType` (TOPUP, PAYMENT, REFUND, WITHDRAWAL)
- Relationships:
  - N:1 với `Wallet`
  - N:1 với `Order` (nullable)

### 4. **Shop** (`shops` table)
- Cửa hàng của từng người bán
- Relationships:
  - 1:1 với `User` (seller)
  - 1:N với `Product`
  - 1:N với `Order`
  - 1:N với `RevenueSnapshot`

### 5. **Category** (`categories` table)
- Danh mục sản phẩm (có hỗ trợ multi-level)
- Relationships:
  - N:1 với `Category` (parent)
  - 1:N với `Category` (children)
  - 1:N với `Product`

### 6. **Product** (`products` table)
- Sản phẩm bán trên sàn
- Enum: `ProductCondition` (NEW, LIKE_NEW, GOOD, FAIR)
- Full-text search trên `name` + `brand`
- Relationships:
  - N:1 với `Shop`
  - N:1 với `Category`
  - 1:N với `ProductImage`
  - 1:N với `ProductAttribute`
  - 1:N với `ProductTag`
  - 1:N với `Review`
  - 1:N với `Comment`
  - 1:N với `OrderItem`

### 7. **ProductImage** (`product_images` table)
- Hình ảnh sản phẩm
- Relationships:
  - N:1 với `Product`

### 8. **ProductAttribute** (`product_attributes` table)
- Thuộc tính sản phẩm (ví dụ: size, màu sắc, chất liệu)
- Relationships:
  - N:1 với `Product`

### 9. **ProductTag** (`product_tags` table)
- Tags/nhãn cho sản phẩm
- Relationships:
  - N:1 với `Product`

### 10. **Order** (`orders` table)
- Đơn hàng
- Enum: `OrderStatus` (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, REFUNDED)
- Enum: `PaymentMethod` (WALLET, COD, BANK_TRANSFER)
- Enum: `PaymentStatus` (UNPAID, PAID, REFUNDED)
- Relationships:
  - N:1 với `User` (customer)
  - N:1 với `Shop`
  - N:1 với `UserAddress` (shipping address)
  - 1:N với `OrderItem`
  - 1:N với `OrderStatusLog`
  - 1:N với `Review`
  - N:1 với `WalletTransaction`

### 11. **OrderItem** (`order_items` table)
- Chi tiết từng sản phẩm trong đơn hàng
- Relationships:
  - N:1 với `Order`
  - N:1 với `Product` (nullable, nếu sản phẩm bị xóa)

### 12. **OrderStatusLog** (`order_status_logs` table)
- Lịch sử thay đổi trạng thái đơn hàng
- Relationships:
  - N:1 với `Order`
  - N:1 với `User` (who changed, nullable)

### 13. **Review** (`reviews` table)
- Đánh giá/nhận xét sản phẩm từ khách hàng
- Rating: 1-5 (check constraint)
- Relationships:
  - N:1 với `Product`
  - N:1 với `User` (customer)
  - N:1 với `Order`
  - 1:N với `ReviewImage`

### 14. **ReviewImage** (`review_images` table)
- Hình ảnh đi kèm đánh giá
- Relationships:
  - N:1 với `Review`

### 15. **Comment** (`comments` table)
- Bình luận/thảo luận sản phẩm
- Hỗ trợ comment nested (reply)
- Relationships:
  - N:1 với `Product`
  - N:1 với `User`
  - N:1 với `Comment` (parent, nullable)

### 16. **UserAddress** (`user_addresses` table)
- Địa chỉ giao hàng của user
- Relationships:
  - N:1 với `User`
  - 1:N với `Order` (shipping address)

### 17. **RevenueSnapshot** (`revenue_snapshots` table)
- Snapshot doanh thu hàng ngày cho từng cửa hàng
- Relationships:
  - N:1 với `Shop`

---

## Danh sách Enum (7 enums)

| Enum | Giá trị | Mục đích |
|------|--------|---------|
| `UserRole` | CUSTOMER, SELLER, ADMIN | Vai trò người dùng |
| `AuthProvider` | EMAIL, GOOGLE, FACEBOOK | Cách đăng nhập |
| `ProductCondition` | NEW, LIKE_NEW, GOOD, FAIR | Tình trạng sản phẩm cũ |
| `OrderStatus` | PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, REFUNDED | Trạng thái đơn hàng |
| `PaymentMethod` | WALLET, COD, BANK_TRANSFER | Phương thức thanh toán |
| `PaymentStatus` | UNPAID, PAID, REFUNDED | Trạng thái thanh toán |
| `TransactionType` | TOPUP, PAYMENT, REFUND, WITHDRAWAL | Loại giao dịch ví |

---

## Danh sách Repository (17 repositories)

| Repository | Chức năng | Queries đặc biệt |
|------------|---------|------------------|
| `UserRepository` | Quản lý user | findByEmail, findByProviderIdAndAuthProvider |
| `WalletRepository` | Quản lý ví | findByUserId |
| `WalletTransactionRepository` | Ghi nhận giao dịch ví | findByWalletId, findByOrderId (phân trang) |
| `ShopRepository` | Quản lý cửa hàng | findBySlug, findBySellerId |
| `CategoryRepository` | Quản lý danh mục | findByParentIsNullAndIsActiveTrue, findByParentIdAndIsActiveTrue |
| `ProductRepository` | Quản lý sản phẩm | searchByKeyword (full-text), findByCondition, findByShopId |
| `ProductImageRepository` | Quản lý ảnh sản phẩm | findByProductIdOrderBySortOrder, findByProductIdAndIsPrimaryTrue |
| `ProductAttributeRepository` | Quản lý thuộc tính | findByProductId |
| `ProductTagRepository` | Quản lý tags | findByProductId, countByTag |
| `OrderRepository` | Quản lý đơn hàng | findByCustomerId, findByShopId, findByStatus (phân trang) |
| `OrderItemRepository` | Chi tiết đơn hàng | findByOrderId, findByProductId |
| `OrderStatusLogRepository` | Lịch sử trạng thái | findByOrderIdOrderByCreatedAtDesc |
| `ReviewRepository` | Quản lý đánh giá | findByProductId, findByUserId, findByOrderIdAndProductId (phân trang) |
| `ReviewImageRepository` | Hình ảnh đánh giá | findByReviewId |
| `CommentRepository` | Quản lý bình luận | findByProductIdAndParentIsNullAndIsVisibleTrue (phân trang) |
| `UserAddressRepository` | Quản lý địa chỉ | findByUserId, findByUserIdAndIsDefaultTrue |
| `RevenueSnapshotRepository` | Thống kê doanh thu | findByShopIdAndSnapshotDateBetween (phân trang) |

---

## Đặc điểm kỹ thuật

### ID Strategy
- Sử dụng **UUID** (CHAR(36))
- Format: MySQL UUID() hoặc Java `UUID.randomUUID().toString()`

### Timestamps
- `createdAt`: tự động khi tạo, không cập nhật
- `updatedAt`: tự động khi tạo, cập nhật mỗi khi sửa

### Indexes
- Tất cả foreign key đều có index
- Các trường tìm kiếm thường xuyên có index
- Full-text search trên Product(`name`, `brand`)

### Constraints
- Unique constraints trên email, slug, order code, shop slug
- Check constraint: rating 1-5, wallet balance >= 0

### Cascade
- Delete cascade cho hầu hết relationships con
- Set NULL cho parent_id khi parent bị xóa (category, comment)

---

## Hướng sử dụng

### 1. Tạo entity từ repository
```java
@Autowired
private UserRepository userRepository;

User user = User.builder()
    .id(UUID.randomUUID().toString())
    .email("user@example.com")
    .fullName("John Doe")
    .role(UserRole.CUSTOMER)
    .build();

userRepository.save(user);
```

### 2. Tìm kiếm
```java
// Tìm user theo email
Optional<User> user = userRepository.findByEmail("test@example.com");

// Tìm sản phẩm theo category
Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);

// Full-text search
Page<Product> searchResults = productRepository.searchByKeyword("áo sơ mi", pageable);
```

### 3. Liên kết relationships
```java
// Khi tạo Product, liên kết với Shop và Category
Product product = Product.builder()
    .shop(shop)
    .category(category)
    .name("Áo sơ mi nam")
    .build();
```

---

## Quy ước tên bảng/cột

- Tên bảng: **snake_case, số nhiều** (vd: `users`, `product_images`)
- Tên cột: **snake_case** (vd: `created_at`, `is_active`)
- Foreign key: **fk_table_name** (vd: `fk_products_shop`)
- Unique index: **uq_table_name_column** (vd: `uq_users_email`)
- Regular index: **idx_table_name_column** (vd: `idx_products_shop`)

---

## Kiểm tra compile

Tất cả entity và repository đã được kiểm tra qua Maven test:
```
Found 17 JPA repository interfaces.
```

✅ Hệ thống entity hoàn toàn sẵn sàng để sử dụng.

